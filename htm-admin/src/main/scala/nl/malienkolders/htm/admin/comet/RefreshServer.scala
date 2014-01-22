package nl.malienkolders.htm.admin.comet

import net.liftweb._
import http._
import actor._

object RefreshServer extends LiftActor with ListenerManager {

  def createUpdate = RefreshMessage
  
  def scheduleChanged() = {
    this ! RefreshMessage(S.session.get.httpSession.get.sessionId)    
  }

  override def lowPriority = {
  	case _ =>
      updateListeners()
  }
}
