package nl.malienkolders.htm.admin.comet

import net.liftweb._
import http._
import actor._

object ChatServer extends LiftActor with ListenerManager {
  private var msgs: InboxMessages = InboxMessages(Vector())

  def createUpdate = msgs

  override def lowPriority = {
    case s: String =>
      msgs = InboxMessages(msgs.v ++ Vector(System.currentTimeMillis() -> s))
      updateListeners()
  }
}

case class InboxMessages(v: Vector[(Long, String)])