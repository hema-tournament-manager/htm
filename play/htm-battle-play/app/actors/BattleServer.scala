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

  import BattleServerMsgs._
  
  implicit val formats = Serialization.formats(NoTypeHints)
  
   private var currentRound: Option[MarshalledRound] = None
  private var currentPool: Option[MarshalledPoolSummary] = None
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

    case RequestCurrentFight => sender ! (currentRound, currentPool)
    
    case SetCurrentFight(pool) => {
      currentPool = Some(pool)
      val roundReq = :/(adminHost) / "api" / "round" / pool.round.id.toString
      val round = Http(roundReq OK as.String).fold[Option[MarshalledRound]](
        _ => None,
        success => Some(parse(success).extract[MarshalledRound])).apply
      println("ROUND: " + round)
      if (round.isDefined) {
        currentRound = Some(round.get)
      }
      timer = 0
      timerRunning = false
      breakTimeReached = false

      viewerServer ! InitFight(currentRound, currentPool)
      viewerServer ! StopTimer(timer)
      viewerServer ! UpdateScores(currentScore)
      viewerServer ! ShowView(new FightView)

      sender ! (currentRound, currentPool)
    }
  
    case FightUpdate(fight) => {
      val req = :/(adminHost) / "api" / "fight" / "update" <:< Map("Content-Type" -> "application/json")
      Http(req.POST << Serialization.write(fight)).apply
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
          viewerServer ! InitFight(currentRound, currentPool)
          viewerServer ! UpdateScores(currentScore)
        case _ =>
        // ignore
      }
    case msg =>
      println(msg.toString)
  }
 
}
object BattleServerMsgs {
  case class BattleServerUpdate(currentRound: Option[MarshalledRound], currentPool: Option[MarshalledPoolSummary], nextFight: Option[MarshalledFight], currentTime: Long, scores: List[Score])
case object RequestCurrentPool
case class SubscribePool(pool: MarshalledPoolSummary)
case class UnsubscribePool(pool: MarshalledPoolSummary)
case class PoolSubscription(pool: MarshalledPoolSummary)
case object RequestCurrentFight
case class SetCurrentFight(pool: MarshalledPoolSummary)
case class FightUpdate(fight: MarshalledFight)
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
}

