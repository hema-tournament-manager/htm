package nl.malienkolders.htm.battle
package snippet

import comet._
import net.liftweb._
import http._
import common._
import util._
import Helpers._
import js._
import JsCmds._
import nl.malienkolders.htm.lib._

object ScreenController {

  def render =
    "#radio1 [onclick]" #> SHtml.ajaxInvoke { () =>
      ViewerServer ! ShowView(new EmptyView)
      S.notice("Switched to empty screen")
      Noop
    } &
      "#radio2 [onclick]" #> SHtml.ajaxInvoke { () =>
        BattleServer ! UpdateViewer(new PoolOverview)
        ViewerServer ! ShowView(new PoolOverview)
        S.notice("Switched to poule overview")
        Noop
      } &
      "#radio3 [onclick]" #> SHtml.ajaxInvoke { () =>
        BattleServer ! UpdateViewer(new PoolRanking)
        ViewerServer ! ShowView(new PoolRanking)
        S.notice("Switched to poule ranking")
        Noop
      } &
      "#radio4 [onclick]" #> SHtml.ajaxInvoke { () =>
        BattleServer ! UpdateViewer(new FightView)
        ViewerServer ! ShowView(new FightView)
        S.notice("Switched to fight")
        Noop
      }

}