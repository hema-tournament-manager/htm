package nl.malienkolders.htm.admin.comet

import net.liftweb._
import http._
import actor._
import nl.malienkolders.htm.lib.model.SavedMessage
import nl.malienkolders.htm.lib.model.SaveListenerRegistry

object RefreshServer extends LiftActor with ListenerManager {

  SaveListenerRegistry.addListener(notifyClients _)

  def createUpdate = Nil

  def notifyClients = {

    for {
      s <- S.session
    } {
      updateListeners(RefreshMessage(s.uniqueId));
    }
  }
}
