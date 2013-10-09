package actors

import akka.actor.Actor
import akka.actor.Props
import nl.malienkolders.htm.lib._
import nl.malienkolders.htm.lib.model._
import dispatch._
import Http._
import scala.concurrent._
import ExecutionContext.Implicits.global
import net.liftweb.json._
import java.util.Date

class BattleServer extends Actor {

  implicit val formats = Serialization.formats(NoTypeHints)
  
   private var currentRound: Option[MarshalledRound] = None
  private var currentPool: Option[MarshalledPoolSummary] = None
  private var currentFight: Option[MarshalledFight] = None
  private var timer: Long = 0
  private var lastTimerStart: Long = -1
  private var timerRunning = false
  private var fightStartedAt: Long = -1
  private var scores: List[Score] = List()
  private var breakTimeReached = false
  lazy val viewerServer = context.actorSelection("viewerServer")
  
  def currentScore = scores.foldLeft(TotalScore(0, 0, 0, 0, 0, 0, 0, 0)) { case (TotalScore(a, aa, b, ba, d, as, bs, x), s) => TotalScore(a + s.a, aa + s.aAfter, b + s.b, ba + s.bAfter, d + s.double, as, bs, x + (if (s.isExchange) 1 else 0)) }

  def exchangeCount = scores.count(_.isExchange)
  
  val adminHost = "localhost:8079"
  
  override def preStart(): Unit = {
    println("BATTLESERVER STARTED")
  }
    
  def receive = {
    case RequestCurrentPool => {
      sender ! currentPool
    }
    
    case SubscribePool(pool) => {
      currentPool = Some(pool)
    }
      
    case UnsubscribePool(pool) =>
      currentPool = None
      
    case PoolSubscription(pool) => sender ! (currentPool.isDefined && currentPool.get == pool)

    case RequestCurrentFight => sender ! (currentRound, currentPool, currentFight)
    
    case SetCurrentFight(pool) => {
      currentPool = Some(pool)
      val roundReq = :/(adminHost) / "api" / "round" / pool.round.id.toString
      val round = Http(roundReq OK as.String).fold[Option[MarshalledRound]](
        _ => None,
        success => Some(parse(success).extract[MarshalledRound])).apply
      println("ROUND: " + round)
      if (round.isDefined) {
        currentRound = Some(round.get)
        val req = :/(adminHost) / "api" / "pool" / pool.id.toString / "fight" / "pop"
        currentFight = Http(req OK as.String).fold[Option[MarshalledFight]](
          _ => None,
          success => Some(Serialization.read[MarshalledFight](success))).apply
      }
      timer = 0
      timerRunning = false
      breakTimeReached = false

      viewerServer ! InitFight(currentRound, currentPool, currentFight)
      viewerServer ! StopTimer(timer)
      viewerServer ! UpdateScores(currentScore)
      viewerServer ! ShowView(new FightView)

      if (currentFight.isDefined) {
        try {
          val req = :/(adminHost) / "api" / "pool" / pool.id.toString / "fight" / "peek"
          val nextFight = Http(req OK as.String).fold[Option[MarshalledFight]](
            _ => None,
            success => success match {
              case "false" => None
              case _ => Some(Serialization.read[MarshalledFight](success))
            }).apply
          viewerServer ! ShowMessage(
            nextFight match {
              case Some(f) => "Next up: %s (red) vs %s (blue)" format (f.fighterA.shortName, f.fighterB.shortName)
              case _ => ""
            }, -1)
        } catch {
          case _: Throwable => viewerServer ! ShowMessage("", -1)
        }
      }

      sender ! (currentRound, currentPool, currentFight)
    }
  
  case s: Score => {
      if (!s.isExchange || currentRound.get.exchangeLimit == 0 || exchangeCount < currentRound.get.exchangeLimit) {
        scores = s :: scores
        viewerServer ! UpdateScores(currentScore)
      }
    }
    case Undo => {
      scores = scores.drop(1)
      viewerServer ! UpdateScores(currentScore)
    }
    case Cancel => {
      if (timerRunning) {
        timerRunning = false
        timer += System.currentTimeMillis - lastTimerStart
      }

      val req = :/(adminHost) / "api" / "fight" / "cancel" <:< Map("Content-Type" -> "application/json")
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
        currentFight = None
        timer = 0
        timerRunning = false
        scores = List()

        viewerServer ! ShowMessage("", -1)
        self ! UpdateViewer(new PoolOverview)
        viewerServer ! ShowView(new PoolOverview)
      }
    }
    case Finish => {
      if (timerRunning) {
        timerRunning = false
        timer += System.currentTimeMillis - lastTimerStart
      }

      val req = :/(adminHost) / "api" / "fight" / "confirm" <:< Map("Content-Type" -> "application/json")
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
        currentFight = None
        timer = 0
        timerRunning = false
        scores = List()

        viewerServer ! ShowMessage("", -1)
        self ! UpdateViewer(new PoolOverview)
        viewerServer ! ShowView(new PoolOverview)
      }
    }
    case UpdateViewer(v) =>
      v match {
        case _: PoolOverview =>
          viewerServer ! InitPoolOverview(
            currentPool.map { cp =>
              val req = :/(adminHost) / "api" / "pool" / cp.id.toString / "viewer"
              Http(req OK as.String).fold(
                _ => None,
                success => Some(Serialization.read[MarshalledViewerPool](success))).apply
            }.getOrElse(None))
        case _: PoolRanking =>
          viewerServer ! InitPoolRanking(
            currentPool.map { cp =>
              val req = :/(adminHost) / "api" / "pool" / cp.id.toString / "ranking"
              Http(req OK as.String).fold(
                _ => None,
                success => Some(Serialization.read[MarshalledPoolRanking](success))).apply
            }.getOrElse(None))
        case _: FightView =>
          viewerServer ! InitFight(currentRound, currentPool, currentFight)
          viewerServer ! UpdateScores(currentScore)
        case _ =>
        // ignore
      }
    case msg =>
      println(msg.toString)
  }
}

case class BattleServerUpdate(currentRound: Option[MarshalledRound], currentPool: Option[MarshalledPoolSummary], currentFight: Option[MarshalledFight], nextFight: Option[MarshalledFight], currentTime: Long, scores: List[Score])
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