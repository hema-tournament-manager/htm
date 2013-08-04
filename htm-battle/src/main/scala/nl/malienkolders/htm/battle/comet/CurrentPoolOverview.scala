package nl.malienkolders.htm.battle
package comet

import model._
import snippet._
import net.liftweb._
import common._
import http._
import dispatch._
import util._
import Helpers._
import js._
import JsCmds._
import json._
import scala.xml.Text
import nl.malienkolders.htm.lib.model._

class CurrentPoolOverview extends CometActor with CometListener {

  override def defaultPrefix = Full("comet")
  def registerWith = BattleServer

  var currentPool: Box[MarshalledPoolSummary] = Empty

  def render = {
    implicit val formats = Serialization.formats(NoTypeHints)

    def renderScore(f: MarshalledViewerFightSummary) = {
      if (f.finished) {
        f.score match {
          case TotalScore(a, _, b, _, d, _, _, _) =>
            "%d (%d) %d" format (a, d, b)
        }
      } else if (f.started) {
        "current"
      } else {
        "vs"
      }
    }

    currentPool.map { p =>
      val req = :/(ShowAdminConnection.adminHost) / "api" / "pool" / p.id.toString / "viewer"
      val vp = Http(req OK as.String).fold(
        _ => Empty,
        success => Full(Serialization.read[MarshalledViewerPool](success))).apply
      vp match {
        case Full(p) => ".fight" #> p.fights.map(f =>
          ".order *" #> f.order &
            ".red *" #> f.fighterA.shortName &
            ".blue *" #> f.fighterB.shortName &
            ".score *" #> renderScore(f))
        case _ => "*" #> ""
      }
    }.getOrElse[CssSel]("*" #> "")
  }

  override def lowPriority = {
    case BattleServerUpdate(_, p, _, _, _, _) => {
      currentPool = p
      reRender(true)
    }
    case _ => //ignore
  }

}