package nl.malienkolders.htm.admin.snippet

import net.liftweb._
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.sitemap._
import net.liftweb.util.Helpers._
import net.liftweb.http.js._
import net.liftweb.http.js.JsCmds._
import net.liftweb.mapper._
import nl.malienkolders.htm.lib.model._
import nl.malienkolders.htm.lib.HtmHelpers._
import nl.malienkolders.htm.admin.lib._
import nl.malienkolders.htm.admin.lib.TournamentUtils._
import net.liftweb.http.SHtml.ElemAttr.pairToBasic
import net.liftweb.sitemap.LocPath.stringToLocPath
import net.liftweb.util.IterableConst.itBindable
import net.liftweb.util.IterableConst.itNodeSeqFunc
import net.liftweb.util.IterableConst.itStringPromotable
import net.liftweb.util.StringPromotable.jsCmdToStrPromo
import scala.xml.Text
import scala.xml.EntityRef
import scala.xml.EntityRef
import scala.collection.mutable.ListBuffer
import scala.util.Random
import nl.malienkolders.htm.lib.SwissTournament

object TournamentEdit {
  val menu = Menu.param[ParamInfo]("Edit Tournament", "Edit Tournament", s => Full(ParamInfo(s)),
    pi => pi.param) / "tournaments" / "edit"
  lazy val loc = menu.toLoc

  def render = {
    val t = Tournament.find(By(Tournament.identifier, TournamentEdit.loc.currentValue.map(_.param).get)).get

    ".tournamentIdentifier *" #> t.identifier &
      ".tournamentNameOld *" #> t.name &
      ".tournamentNameNew" #> (SHtml.text(t.name.get, t.name(_)) ++ SHtml.hidden { () =>
        t.save
        S.redirectTo("/tournaments/list")
      })

  }

}