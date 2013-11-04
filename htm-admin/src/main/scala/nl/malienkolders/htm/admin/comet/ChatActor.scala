package nl.malienkolders.htm.admin.comet

import net.liftweb.http._
import net.liftweb.common._
import net.liftweb.util.ClearClearable
import xml.NodeSeq
import js.{ JE, JsCmd }
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

class ChatActor extends CometActor with CometListener with Loggable {

  private var msgs: InboxMessages = InboxMessages(Vector())

  def registerWith = ChatServer

  override def lowPriority = {

    case InboxMessages(v) =>
      sendListOrLastMessage(v)
      msgs = InboxMessages(v)

    case InitialRender =>
      partialUpdate(InitialMessages(msgs.v))

  }

  def render = {
    ClearClearable
  }

  override def fixedRender: Box[NodeSeq] = {
    this ! InitialRender
    NodeSeq.Empty
  }

  private[this] def sendListOrLastMessage(v: Vector[(Long, String)]) = {
    if ((v.length - msgs.v.length) > 1) {
      this ! InitialRender
    } else {
      partialUpdate(NewMessage(v.last._1, v.last._2))
    }
  }
}

case class NewMessage(time: Long, message: String) extends JsCmd {
  implicit val formats = DefaultFormats.lossless
  val json: JValue = ("time" -> time) ~ ("message" -> message)
  override val toJsCmd = JE.JsRaw(""" $(document).trigger('new-chat-message', %s)""".format(compact(render(json)))).toJsCmd
}

case class InitialMessages(messages: Vector[(Long, String)]) extends JsCmd {
  implicit val formats = DefaultFormats.lossless
  val json: JValue = messages.map {
    case (time, message) =>
      ("time" -> time) ~ ("message" -> message)
  }
  override val toJsCmd = JE.JsRaw(""" $(document).trigger('initial-chat-messages', %s)""".format(compact(render(json)))).toJsCmd
}

case object InitialRender