package nl.malienkolders.htm.battle
package comet

import model._
import net.liftweb._
import common._
import http._
import util._
import Helpers._
import js._
import JsCmds._
import scala.xml.Text
import nl.malienkolders.htm.lib.model._
import nl.malienkolders.htm.lib.HtmHelpers._

class ScoreHistory extends CometActor with CometListener {

  override def defaultPrefix = Full("comet")
  def registerWith = BattleServer

  var scores: List[Score] = List()

  def render = {
    "#scoreHistLine *" #> {
      var exchangeCounter = 0
      scores.reverse.map { s =>
        ".histExchange *" #> (if (s.isExchange) { exchangeCounter += 1; exchangeCounter.toString } else "-") &
          ".histT *" #> renderTime(s.timeInFight) &
          ".histA *" #> s.a &
          ".histBA *" #> s.aAfter &
          ".histB *" #> s.b &
          ".histAB *" #> s.bAfter &
          ".histD *" #> s.double &
          ".histRemark *" #> s.remark
      }
    }

  }

  override def lowPriority = {
    case BattleServerUpdate(_, _, _, _, _, ss) =>
      scores = ss
      reRender(true)
    case ScoreUpdate(ss) =>
      scores = ss
      reRender(true)
    case _ => //ignore
  }

}