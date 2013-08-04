package nl.malienkolders.htm.battle
package comet

import snippet._
import _root_.net.liftweb._
import http._
import common._
import actor._
import util._
import Helpers._
import _root_.scala.xml.{ NodeSeq, Text }
import _root_.java.util.Date
import nl.malienkolders.htm.lib.model._
import dispatch._
import Http._
import net.liftweb.json._
import nl.malienkolders.htm.lib._

object BattleServer extends LiftActor with ListenerManager {

  private var currentRound: Box[MarshalledRound] = Empty
  private var currentPool: Box[MarshalledPoolSummary] = Empty
  private var currentFight: Box[MarshalledFight] = Empty
  private var timer: Long = 0
  private var lastTimerStart: Long = -1
  private var timerRunning = false
  private var fightStartedAt: Long = -1
  private var scores: List[Score] = List()
  private var breakTimeReached = false

  implicit val formats = Serialization.formats(NoTypeHints)

  def currentTime = timer + (if (timerRunning) System.currentTimeMillis - lastTimerStart else 0)

  def currentScore = scores.foldLeft(TotalScore(0, 0, 0, 0, 0, 0, 0, 0)) { case (TotalScore(a, aa, b, ba, d, as, bs, x), s) => TotalScore(a + s.a, aa + s.aAfter, b + s.b, ba + s.bAfter, d + s.double, as, bs, x + (if (s.isExchange) 1 else 0)) }

  def exchangeCount = scores.count(_.isExchange)

  override def lowPriority = {
    case RequestCurrentPool => reply(currentPool)
    case SubscribePool(pool) =>
      currentPool = Full(pool)
      updateListeners
    case UnsubscribePool(pool) =>
      currentPool = Empty
      updateListeners
    case PoolSubscription(pool) => reply(currentPool.isDefined && currentPool.get == pool)
    case RequestCurrentFight => reply((currentRound, currentPool, currentFight))
    case SetCurrentFight(pool) => {
      println("SET CURRENT FIGHT")
      currentPool = Full(pool)
      println("POOL: " + pool.toString)
      val roundReq = :/(ShowAdminConnection.adminHost) / "api" / "round" / pool.round.id.toString
      val round = Http(roundReq OK as.String).fold[Option[MarshalledRound]](
        _ => None,
        success => Some(parse(success).extract[MarshalledRound])).apply
      println("ROUND: " + round)
      if (round.isDefined) {
        currentRound = Full(round.get)
        val req = :/(ShowAdminConnection.adminHost) / "api" / "pool" / pool.id.toString / "fight" / "pop"
        currentFight = Http(req OK as.String).fold[Box[MarshalledFight]](
          _ => Empty,
          success => Full(Serialization.read[MarshalledFight](success))).apply
      }
      timer = 0
      timerRunning = false
      breakTimeReached = false

      println("INIT VIEWER")
      ViewerServer ! InitFight(currentRound, currentPool, currentFight)
      ViewerServer ! StopTimer(timer)
      ViewerServer ! UpdateScores(currentScore)
      ViewerServer ! ShowView(new FightView)

      if (currentFight.isDefined) {
        println("PEEK NEXT FIGHT")
        try {
          val req = :/(ShowAdminConnection.adminHost) / "api" / "pool" / pool.id.toString / "fight" / "peek"
          val nextFight = Http(req OK as.String).fold[Box[MarshalledFight]](
            _ => Empty,
            success => success match {
              case "false" => Empty
              case _ => Full(Serialization.read[MarshalledFight](success))
            }).apply
          ViewerServer ! ShowMessage(
            nextFight match {
              case Full(f) => "Next up: %s (red) vs %s (blue)" format (f.fighterA.shortName, f.fighterB.shortName)
              case _ => ""
            }, -1)
        } catch {
          case _: Throwable => ViewerServer ! ShowMessage("", -1)
        }
      }

      println("UPDATE LISTENERS")
      updateListeners
      reply((currentRound, currentPool, currentFight))
    }
    case UpdateTimer => {
      if (!breakTimeReached && (currentRound.get.breakInFightAt > 0) && (currentTime > currentRound.get.breakInFightAt)) {
        breakTimeReached = true
        timerRunning = false
        timer = currentRound.get.breakInFightAt
        ViewerServer ! StopTimer(timer)
        ViewerServer ! UpdateScores(currentScore)
      }
      if ((currentRound.get.timeLimitOfFight > 0) && (currentTime > currentRound.get.timeLimitOfFight)) {
        timerRunning = false
        timer = currentRound.get.timeLimitOfFight
        ViewerServer ! StopTimer(timer)
        ViewerServer ! UpdateScores(currentScore)
      }
      if (timerRunning) {
        Schedule.schedule(this, UpdateTimer, 1 second)
        updateListeners(TimerUpdate(currentTime))
      } else {
        updateListeners
      }
    }
    case Start => {
      if ((currentRound.get.exchangeLimit > 0) && (exchangeCount >= currentRound.get.exchangeLimit)) {
        S.notice("Exchange limit reached!")
      } else if ((currentRound.get.timeLimitOfFight > 0) && (timer >= currentRound.get.timeLimitOfFight)) {
        S.notice("Time limit reached!")
      } else {
        ViewerServer ! UpdateScores(currentScore)
        if (fightStartedAt < 0)
          fightStartedAt = new Date().getTime()
        if (!timerRunning) {
          lastTimerStart = System.currentTimeMillis()
          timerRunning = true
          ViewerServer ! StartTimer(timer)
        }
        this ! UpdateTimer
      }
    }
    case Stop => {
      if (timerRunning) {
        timerRunning = false
        timer += System.currentTimeMillis - lastTimerStart
      }
      ViewerServer ! StopTimer(timer)
      ViewerServer ! UpdateScores(currentScore)
      updateListeners
    }
    case s: Score => {
      if (!s.isExchange || currentRound.get.exchangeLimit == 0 || exchangeCount < currentRound.get.exchangeLimit) {
        scores = s :: scores
        ViewerServer ! UpdateScores(currentScore)
        updateListeners(ScoreUpdate(scores))
      }
    }
    case Undo => {
      scores = scores.drop(1)
      ViewerServer ! UpdateScores(currentScore)
      updateListeners(ScoreUpdate(scores))
    }
    case Cancel => {
      if (timerRunning) {
        timerRunning = false
        timer += System.currentTimeMillis - lastTimerStart
      }

      val req = :/(ShowAdminConnection.adminHost) / "api" / "fight" / "cancel" <:< Map("Content-Type" -> "application/json")
      val f = currentFight.get
      val result = Http(req.POST << Serialization.write(MarshalledFight(
        f.id,
        f.pool,
        f.round,
        f.order,
        f.fighterA,
        f.fighterB,
        fightStartedAt,
        new Date().getTime(),
        timer,
        List()))).fold[Boolean](_ => false, resp => resp.getResponseBody().toBoolean).apply

      if (result) {
        currentFight = Empty
        timer = 0
        timerRunning = false
        scores = List()

        ViewerServer ! ShowMessage("", -1)
        BattleServer ! UpdateViewer(new PoolOverview)
        ViewerServer ! ShowView(new PoolOverview)
      }

      updateListeners
    }
    case Finish => {
      if (timerRunning) {
        timerRunning = false
        timer += System.currentTimeMillis - lastTimerStart
      }

      val req = :/(ShowAdminConnection.adminHost) / "api" / "fight" / "confirm" <:< Map("Content-Type" -> "application/json")
      val f = currentFight.get
      val result = Http(req.POST << Serialization.write(MarshalledFight(
        f.id,
        f.pool,
        f.round,
        f.order,
        f.fighterA,
        f.fighterB,
        fightStartedAt,
        new Date().getTime(),
        timer,
        scores.map(s => MarshalledScore(
          s.timeInFight,
          s.timeInWorld,
          s.a,
          s.b,
          s.aAfter,
          s.bAfter,
          s.double,
          s.remark,
          s.isSpecial,
          s.isExchange))))).fold[Boolean](_ => false, resp => resp.getResponseBody().toBoolean).apply

      if (result) {
        currentFight = Empty
        timer = 0
        timerRunning = false
        scores = List()

        ViewerServer ! ShowMessage("", -1)
        BattleServer ! UpdateViewer(new PoolOverview)
        ViewerServer ! ShowView(new PoolOverview)
      }

      updateListeners
    }
    case UpdateViewer(v) =>
      v match {
        case _: PoolOverview =>
          ViewerServer ! InitPoolOverview(
            currentPool.map { cp =>
              val req = :/(ShowAdminConnection.adminHost) / "api" / "pool" / cp.id.toString / "viewer"
              Http(req OK as.String).fold(
                _ => Empty,
                success => Full(Serialization.read[MarshalledViewerPool](success))).apply
            }.getOrElse(Empty))
        case _: PoolRanking =>
          ViewerServer ! InitPoolRanking(
            currentPool.map { cp =>
              val req = :/(ShowAdminConnection.adminHost) / "api" / "pool" / cp.id.toString / "ranking"
              Http(req OK as.String).fold(
                _ => Empty,
                success => Full(Serialization.read[MarshalledPoolRanking](success))).apply
            }.getOrElse(Empty))
        case _: FightView =>
          ViewerServer ! InitFight(currentRound, currentPool, currentFight)
          ViewerServer ! UpdateScores(currentScore)
        case _ =>
        // ignore
      }
    case msg =>
      println(msg.toString)
  }

  def peek(p: MarshalledPoolSummary) = {
    val req = :/(ShowAdminConnection.adminHost) / "api" / "pool" / p.id.toString / "fight" / "peek"
    Http(req OK as.String).fold[Box[MarshalledFight]](
      _ => Empty,
      success => success match {
        case "false" => Empty
        case _ => Full(Serialization.read[MarshalledFight](success))
      }).apply
  }

  def nextFight = currentPool.map(p => peek(p)).getOrElse(Empty)

  def createUpdate = BattleServerUpdate(currentRound, currentPool, currentFight, nextFight, timer, scores)

}

case class BattleServerUpdate(currentRound: Box[MarshalledRound], currentPool: Box[MarshalledPoolSummary], currentFight: Box[MarshalledFight], nextFight: Box[MarshalledFight], currentTime: Long, scores: List[Score])
case object RequestCurrentPool
case class SubscribePool(pool: MarshalledPoolSummary)
case class UnsubscribePool(pool: MarshalledPoolSummary)
case class PoolSubscription(pool: MarshalledPoolSummary)
case object RequestCurrentFight
case class SetCurrentFight(pool: MarshalledPoolSummary)
case class UpdateViewer(v: View)
case object UpdateTimer
case object Start
case object Stop
case object Undo
case object Finish
case object Cancel
case class TimerUpdate(time: Long)
case class ScoreUpdate(scores: List[Score])

case class Score(a: Int, aAfter: Int, b: Int, bAfter: Int, double: Int, timeInWorld: Long, timeInFight: Long, remark: String, isSpecial: Boolean, isExchange: Boolean)
case class ScorePoints(a: Int, aAfter: Int, b: Int, bAfter: Int, double: Int, remark: String, isSpecial: Boolean, isExchange: Boolean)
