package nl.malienkolders.htm.battle
package snippet

import comet._
import net.liftweb._
import common._
import util._
import Helpers._

class CurrentOrNextFight {

  def render = {
    val rcf = BattleServer !! RequestCurrentFight
    rcf match {
      case Full((Full(_), Full(_), Full(_))) => "#currentFight ^^" #> "*"
      case _ => "#pickFight ^^" #> "*"
    }
  }

}