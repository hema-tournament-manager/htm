package actors

import akka.actor.Actor
import akka.actor.Props
import nl.malienkolders.htm.lib._
import nl.malienkolders.htm.lib.model._

class ViewerServer extends Actor {
	
  def receive = {
    case _ => 
  }
  
}

abstract class ViewerMessage
case class StartTimer(time: Long) extends ViewerMessage
case class StopTimer(time: Long) extends ViewerMessage
case class UpdateScores(scores: TotalScore) extends ViewerMessage
case class InitFight(round: Option[MarshalledRound], pool: Option[MarshalledPoolSummary], fight: Option[MarshalledFight]) extends ViewerMessage
case class InitPoolOverview(pool: Option[MarshalledViewerPool]) extends ViewerMessage
case class InitPoolRanking(pool: Option[MarshalledPoolRanking]) extends ViewerMessage
case class ShowView(view: View) extends ViewerMessage
case class ShowMessage(text: String, duration: Long) extends ViewerMessage