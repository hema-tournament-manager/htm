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

class CurrentPool extends CometActor with CometListener {

  override def defaultPrefix = Full("comet")
  def registerWith = BattleServer

  var currentPool: Box[MarshalledPoolSummary] = Empty

  def render = {
    "a *" #> currentPool.map(p =>
      p.round.tournament.name + " | " + p.round.name + " | Poule " + p.order).getOrElse("<Click to subscribe>")
  }

  override def lowPriority = {
    case BattleServerUpdate(_, p, _, _, _, _) => {
      currentPool = p
      reRender(true)
    }
    case _ => //ignore
  }

}