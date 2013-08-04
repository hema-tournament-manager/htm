package nl.malienkolders.htm.battle
package comet

import net.liftweb._
import actor._
import http._
import common._
import nl.malienkolders.htm.lib._
import nl.malienkolders.htm.lib.model._

object ViewerServer extends LiftActor with ListenerManager {

  override def lowPriority = {
    case msg =>
      updateListeners(msg)
  }

  def createUpdate = "update"

}

abstract class ViewerMessage
case class StartTimer(time: Long) extends ViewerMessage
case class StopTimer(time: Long) extends ViewerMessage
case class UpdateScores(scores: TotalScore) extends ViewerMessage
case class InitFight(round: Box[MarshalledRound], pool: Box[MarshalledPoolSummary], fight: Box[MarshalledFight]) extends ViewerMessage
case class InitPoolOverview(pool: Box[MarshalledViewerPool]) extends ViewerMessage
case class InitPoolRanking(pool: Box[MarshalledPoolRanking]) extends ViewerMessage
case class ShowView(view: View) extends ViewerMessage
case class ShowMessage(text: String, duration: Long) extends ViewerMessage

