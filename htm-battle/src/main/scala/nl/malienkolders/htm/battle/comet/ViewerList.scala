package nl.malienkolders.htm.battle
package comet
import _root_.net.liftweb._
import http._
import common._
import actor._
import util._
import Helpers._
import _root_.scala.xml._
import S._
import SHtml._
import js._
import JsCmds._
import JE._
import model._
import net.liftweb.json.DefaultFormats
import dispatch._
import Http._

class ViewerList extends CometActor with CometListener {

  override def defaultPrefix = Full("comet")

  var viewers = Viewer.findAll()

  def render = {
    viewers = Viewer.findAll

    var selectedScreen = ""

    def selectScreen(v: Viewer, s: String) = {
      asInt(s) match {
        case Full(sIdx) =>
          v.screen(sIdx).save
          v.rest.changeScreen(sIdx)
        case _ =>
      }
      Noop
    }

    def selectMode(v: Viewer, m: String) = {
      v.mode(m).save
      reRender(true)
      Noop
    }

    def togglePower(v: Viewer, running: Boolean) = {
      if (v.screen.get > -99) {
        if (running)
          v.rest.shutdown
        else
          v.rest.boot
      }
      reRender(true)
      Noop
    }
    def disconnect(v: Viewer) = {
      v.delete_!
      reRender(true)
      Noop
    }

    "ul *" #> viewers.map { v =>
      val screens = v.rest.poll
      val indexedScreens = screens.zip(0 to screens.size)
      ".alias" #> <span title={ v.hostname.get + ":" + v.port.get }>{ v.alias.get }</span> &
        ".mode" #> v.mode.is &
        ".screen" #> {
          val state = v.rest.state
          SHtml.ajaxSelect(("-1", "Windowed mode") :: indexedScreens.map(s => (s._2.toString, "Screen %d (%dx%d)" format (s._2 + 1, s._1.width, s._1.height))), Full(v.screen.is.toString), selectScreen(v, _), "id" -> ("screens_" + v.alias)) ++
            SHtml.ajaxButton(if (state) "Power off" else "Power on", () => togglePower(v, state), "id" -> "test") ++
            SHtml.ajaxButton("Disconnect", () => disconnect(v))
        }
    }
  }

  override def lowPriority = {
    case StartTimer(time) => viewers.foreach(_.rest.timer.start(time))
    case StopTimer(time) => viewers.foreach(_.rest.timer.stop(time))
    case UpdateScores(scores) => viewers.foreach(_.rest.fight.score(scores))
    case InitFight(Full(round), Full(pool), Full(fight)) => viewers.foreach(_.rest.fight.init(round, pool, fight))
    case InitPoolOverview(Full(pool)) => viewers.foreach(_.rest.pool.init(pool))
    case InitPoolRanking(Full(pool)) => viewers.foreach(_.rest.pool.ranking(pool))
    case ShowView(view) => viewers.foreach(_.rest.switch(view))
    case ShowMessage(text, duration) => viewers.foreach(_.rest.message(text, duration))
    case msg: String => reRender(true)
  }

  def registerWith = ViewerServer

}