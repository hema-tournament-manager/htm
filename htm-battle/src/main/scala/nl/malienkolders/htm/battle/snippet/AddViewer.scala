package nl.malienkolders.htm.battle.snippet

import net.liftweb._
import http._
import common._
import util.Helpers._
import js._
import JsCmds._
import JE._
import scala.xml.NodeSeq
import nl.malienkolders.htm.battle.model.Viewer
import nl.malienkolders.htm.battle.comet.ViewerServer

object AddViewer {

  def render = {
    var alias = ""
    var host = ""
    var port = "8081"
    var mode = "spectator_screen"

    def process(): JsCmd = {
      asInt(port) match {
        case Full(p) => {
          val v = new Viewer
          v.alias(alias).hostname(host).port(p).mode(mode).screen(-1)
          if (v.rest.ping) {
            v.save()
            ViewerServer ! "update"
            SetValueAndFocus("alias", "") &
              SetValById("host", "")
          } else {
            S.notice("Viewer did not respond")
          }
        }

        case _ => S.error("port", "Port has to be a number"); Noop
      }
    }
    "name=alias" #> SHtml.text(alias, alias = _, "id" -> "alias") &
      "name=host" #> SHtml.text(host, host = _, "id" -> "host") &
      "name=port" #> SHtml.text(port, port = _) &
      "name=mode" #> (SHtml.select(("spectator_screen", "Spectator Screen") :: ("live_stream", "Live Stream") :: Nil, Full(mode), mode = _) ++ SHtml.hidden(process))

  }

}