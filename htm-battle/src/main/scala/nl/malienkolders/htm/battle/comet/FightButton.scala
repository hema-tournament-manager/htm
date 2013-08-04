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
import js.jquery._
import JqJsCmds._
import scala.xml.Text
import nl.malienkolders.htm.lib.model._

class FightButton extends CometActor with CometListener {

  override def defaultPrefix = Full("comet")
  def registerWith = BattleServer

  var currentPool: Box[MarshalledPoolSummary] = Empty
  var currentFight: Box[MarshalledFight] = Empty
  var nextFight: Box[MarshalledFight] = Empty

  def enabled_? = currentFight.isDefined || nextFight.isDefined
  def buttonText = currentPool.map(_ => currentFight.map(_ => "End Fight").getOrElse(nextFight.map(_ => "Next Fight").getOrElse("No Fight :("))).getOrElse("No Poule :(")
  def fightLabel = {
    currentFight.map { f =>
      "Current: %s vs %s" format (f.fighterA.shortName, f.fighterB.shortName)
    } or nextFight.map { f =>
      "Next: %s vs %s" format (f.fighterA.shortName, f.fighterB.shortName)
    } getOrElse { "" }
  }

  def render = {
    def pushTheButton = {
      currentPool.map(p => currentFight.map { _ =>
        Confirm("Are you sure you wish to end the fight and confirm the scores?", SHtml.ajaxInvoke { () =>
          BattleServer ! Finish
          S.notice("Ending the fight")
          Noop
        }._2.cmd)
      }.getOrElse {
        nextFight.map { _ =>
          BattleServer !! SetCurrentFight(p)
          RedirectTo("/fight")
        }.getOrElse {
          S.notice("There are no fights :(")
          Noop
        }
      }).getOrElse {
        S.notice("There are no fights :(")
        Noop
      }
    }

    def pushTheCancelButton = {
      currentFight.map { _ =>
        Confirm("Are you sure you wish to cancel this fight and lose the scores?", SHtml.ajaxInvoke { () =>
          BattleServer ! Cancel
          S.notice("Canceling the fight")
          Noop
        }._2.cmd)
      }.getOrElse {
        Noop
      }
    }

    ".confirm" #> SHtml.ajaxButton(Text(buttonText), () => pushTheButton) &
      ".cancel" #> SHtml.ajaxButton(Text("Cancel Fight"), () => pushTheCancelButton, "style" -> currentFight.map(_ => "").openOr("display: none")) &
      ".fightLabel *" #> fightLabel

  }

  override def lowPriority = {
    case BattleServerUpdate(_, p, f, n, _, _) => {
      currentPool = p
      currentFight = f
      nextFight = n
      partialUpdate(Run("$('#fightButton .confirm span.ui-button-text').text('" + buttonText + "');" +
        "$('#fightButton .cancel')." + (currentFight map (_ => "show") openOr "hide") + "();" +
        "$('#fightButton a.fightLabel').text('" + fightLabel + "');"))
      //reRender(true)
      //      partialUpdate(Run("$('button').button();"))
    }
  }

}