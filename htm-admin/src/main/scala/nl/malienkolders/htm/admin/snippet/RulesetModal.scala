package nl.malienkolders.htm.admin.snippet

import net.liftweb._
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.sitemap._
import Loc._
import net.liftweb.util.Helpers._
import net.liftweb.http.js._
import net.liftweb.http.js.JsCmds._
import net.liftweb.mapper._
import nl.malienkolders.htm.lib.rulesets.Ruleset
import nl.malienkolders.htm.lib.model._
import nl.malienkolders.htm.lib.util.Helpers._
import nl.malienkolders.htm.admin.lib._
import nl.malienkolders.htm.admin.lib.exporter._
import nl.malienkolders.htm.admin.lib.Utils.DateTimeRenderHelper
import nl.malienkolders.htm.lib.util.Helpers._
import net.liftweb.http.SHtml.ElemAttr.pairToBasic
import net.liftweb.sitemap.LocPath.stringToLocPath
import net.liftweb.util.IterableConst.itBindable
import net.liftweb.util.IterableConst.itNodeSeqFunc
import net.liftweb.util.IterableConst.itStringPromotable
import net.liftweb.util.StringPromotable.jsCmdToStrPromo
import scala.xml.Text
import scala.xml.EntityRef
import scala.collection.mutable.ListBuffer
import scala.util.Random
import net.liftweb.http.js.JsCmds.{ Reload, RedirectTo }
import scala.xml.Elem

object RulesetModal {
  val menu = (Menu.param[ParamInfo]("Ruleset", "Ruleset", s => Full(ParamInfo(s)),
    pi => pi.param) / "rulesets" / "modal" >> Hidden)
  lazy val loc = menu.toLoc

  def render = {

    def renderTime(time: Long): String = time.asMinutes match {
      case 0 => "none"
      case 1 => "1 minute"
      case t => s"$t minutes"
    }

    def renderNumber(n: Int): String = n match {
      case 0 => "none"
      case _ => n.toString
    }

    val ruleset = Ruleset(loc.currentValue.get.param)
    ".modal-title *" #> ruleset.id &
      ".item" #> (List[(String, String)](
        "Time limit" -> renderTime(ruleset.fightProperties.timeLimit),
        "Break at" -> renderTime(ruleset.fightProperties.breakAt),
        "Break duration" -> renderTime(ruleset.fightProperties.breakDuration),
        "Time between fights" -> renderTime(ruleset.fightProperties.timeBetweenFights),
        "Exchange limit" -> renderNumber(ruleset.fightProperties.exchangeLimit),
        "Point limit" -> renderNumber(ruleset.fightProperties.pointLimit),
        "Double hit limit" -> renderNumber(ruleset.fightProperties.doubleHitLimit)).map {
          case (key, value) =>
            ".key *" #> key &
              ".value *" #> value
        })

  }
}