package nl.malienkolders.htm.admin.snippet

import net.liftweb._
import common._
import http._
import sitemap._
import Loc._
import util.Helpers._
import nl.malienkolders.htm.lib.model._
import java.text.SimpleDateFormat
import net.liftweb.http.js.JsCmds.Run
import java.util.Date

object FightEdit {

  val menu = (Menu.param[ParamInfo]("Edit Fight", "Edit Fight", s => Full(ParamInfo(s)),
    pi => pi.param) / "fights" / "edit" >> Hidden)
  lazy val loc = menu.toLoc

  def render = {

    val id = FightEdit.loc.currentValue.get.param
    val f: Fight[_, _] = FightHelper.dao(id.take(1)).findByKey(id.drop(1).toLong).get
    val totalScore = f.currentScore

    val df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    def process() {
      f.save()
      S.redirectTo("/tournaments/view/" + f.phase.foreign.get.tournament.is)
    }

    def addScoreLine() {
      f.addScore
      f.save()
      S.redirectTo("/fights/edit/" + f.id.get)
    }

    val dateFormatStr = "yy-mm-dd";
    val timeFormatStr = "HH:mm:ss";

    val datetimepickerInitStr = s"datetimepicker({ dateFormat: '$dateFormatStr', timeFormat: '$timeFormatStr' });";

    S.appendJs(Run("$('#timeStop')." + datetimepickerInitStr) & Run("$('#timeStart')." + datetimepickerInitStr))
    ".red" #> (
      ".name *" #> f.fighterA.participant.get.name.is &
      ".club [title]" #> f.fighterA.participant.get.club.is &
      ".club *" #> f.fighterA.participant.get.clubCode.is) &
      ".blue" #> (
        ".name *" #> f.fighterB.participant.get.name.is &
        ".club [title]" #> f.fighterB.participant.get.club.is &
        ".club *" #> f.fighterB.participant.get.clubCode.is) &
        "#scoreRed" #> totalScore.red &
        "#scoreBlue" #> totalScore.blue &
        "#doAdd" #> SHtml.onSubmitUnit(addScoreLine) &
        "name=timeStart" #> SHtml.text(df.format(new Date(f.timeStart.get)), s => f.timeStart(df.parse(s).getTime()), "id" -> "timeStart", "class" -> "hasDatePicker") &
        "name=timeStop" #> SHtml.text(df.format(new Date(f.timeStop.get)), s => f.timeStop(df.parse(s).getTime()), "id" -> "timeStop", "class" -> "hasDatePicker") &
        ".score" #> f.mapScores(score =>
          "name=pointsRed" #> score.pointsRed.toForm &
            "name=pointsBlue" #> score.pointsBlue.toForm &
            "name=cleanHitsRed" #> score.cleanHitsRed.toForm &
            "name=cleanHitsBlue" #> score.cleanHitsBlue.toForm &
            "name=afterblowsRed" #> score.afterblowsRed.toForm &
            "name=afterblowsBlue" #> score.afterblowsBlue.toForm &
            "name=doubles" #> score.doubles.toForm &
            "name=exchanges" #> score.exchanges.toForm &
            "name=scoreType" #> score.scoreType.toForm) &
        "#doEdit" #> SHtml.onSubmitUnit(process)

  }

}