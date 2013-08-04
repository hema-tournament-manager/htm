package nl.malienkolders.htm.admin.snippet

import net.liftweb._
import common._
import http._
import sitemap._
import util.Helpers._
import nl.malienkolders.htm.lib.model._

object FightEdit {

  val menu = Menu.param[ParamInfo]("Edit Fight", "Edit Fight", s => Full(ParamInfo(s)),
    pi => pi.param) / "fights" / "edit"
  lazy val loc = menu.toLoc

  def render = {
    val f = Fight.findByKey(FightEdit.loc.currentValue.map(_.param).get.toLong).get
    val s = f.currentScore
    ".red" #> (
      ".name *" #> f.fighterA.obj.get.name.is &
      ".club [title]" #> f.fighterA.obj.get.club.is &
      ".club *" #> f.fighterA.obj.get.clubCode.is) &
      ".blue" #> (
        ".name *" #> f.fighterB.obj.get.name.is &
        ".club [title]" #> f.fighterB.obj.get.club.is &
        ".club *" #> f.fighterB.obj.get.clubCode.is) &
        "name=scoreRed" #> SHtml.text(s.a.toString, s => s) &
        "name=scoreBlue" #> SHtml.text(s.b.toString, s => s)

  }

}