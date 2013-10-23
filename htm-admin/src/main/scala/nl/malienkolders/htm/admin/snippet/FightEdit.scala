package nl.malienkolders.htm.admin.snippet

import net.liftweb._
import common._
import http._
import sitemap._
import util.Helpers._
import nl.malienkolders.htm.lib.model._
import java.text.SimpleDateFormat
import net.liftweb.http.js.JsCmds.Run
import java.util.Date

object FightEdit {

  val menu = Menu.param[ParamInfo]("Edit Fight", "Edit Fight", s => Full(ParamInfo(s)),
    pi => pi.param) / "fights" / "edit"
  lazy val loc = menu.toLoc

  def render = {

    val f = Fight.findByKey(FightEdit.loc.currentValue.map(_.param).get.toLong).get
    val totalScore = f.currentScore

    val score = Score.create
    score.diffA(totalScore.a)
    score.diffB(totalScore.b)

    val df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    def process() {
      f.scores.clear()
      f.scores += score
      f.save()
      S.redirectTo("/tournaments/view/" + f.pool.foreign.get.round.foreign.get.tournament.is)
    }

    val dateFormatStr = "yy-mm-dd";
    val timeFormatStr = "HH:mm:ss";

    val datetimepickerInitStr = s"datetimepicker({ dateFormat: '$dateFormatStr', timeFormat: '$timeFormatStr' });";

    S.appendJs(Run("$('#timeStop')." + datetimepickerInitStr) & Run("$('#timeStart')." + datetimepickerInitStr))
    ".red" #> (
      ".name *" #> f.fighterA.obj.get.name.is &
      ".club [title]" #> f.fighterA.obj.get.club.is &
      ".club *" #> f.fighterA.obj.get.clubCode.is) &
      ".blue" #> (
        ".name *" #> f.fighterB.obj.get.name.is &
        ".club [title]" #> f.fighterB.obj.get.club.is &
        ".club *" #> f.fighterB.obj.get.clubCode.is) &
        "name=scoreRed" #> SHtml.text(totalScore.a.toString, s => score.diffA(s.toInt)) &
        "name=scoreBlue" #> SHtml.text(totalScore.b.toString, s => score.diffB(s.toInt)) &
        "name=timeStart" #> SHtml.text(df.format(new Date(f.timeStart.get)), s => f.timeStart(df.parse(s).getTime()), "id" -> "timeStart", "class" -> "hasDatePicker") &
        "name=timeStop" #> SHtml.text(df.format(new Date(f.timeStop.get)), s => f.timeStop(df.parse(s).getTime()), "id" -> "timeStop", "class" -> "hasDatePicker") &
        "#doEdit" #> SHtml.onSubmitUnit(process)

  }

}