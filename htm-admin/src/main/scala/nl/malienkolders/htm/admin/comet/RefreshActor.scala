package nl.malienkolders.htm.admin.comet

import net.liftweb.http._
import net.liftweb.common._
import net.liftweb.util.ClearClearable
import xml.NodeSeq
import js.{ JE, JsCmd, JsCmds }
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

class RefreshActor extends CometActor with CometListener with Loggable {

  def registerWith = RefreshServer

  override def lowPriority = {

    case r: RefreshMessage => {
      println("r.sessionId="+r.sessionId)
      println("S.session.get.uniqueId="+S.session.get.uniqueId)
      if (r.sessionId != S.session.get.uniqueId)
        partialUpdate(r)
    }
  }

  def render = {
    ClearClearable
  }
}

case class RefreshMessage(sessionId: String) extends JsCmd {
  override val toJsCmd = JE.JsRaw(""" $("#reloadNotifyBox").collapse('show') """).toJsCmd
}