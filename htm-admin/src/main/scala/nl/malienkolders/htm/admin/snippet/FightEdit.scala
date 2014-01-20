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
import net.liftweb.http.js.JsCmds._

case class FightEditParams(phaseType: String, fightId: Long);

object FightEdit {

  val menu = (Menu.params[FightEditParams]("Edit Fight", "Edit Fight",
    {
      case phaseType :: fightId :: Nil => Full(FightEditParams(phaseType, fightId.toLong))
      case _ => Empty
    },
    fep => fep.phaseType :: fep.fightId.toString :: Nil) / "fights" / "edit" / ** >> Hidden)
  lazy val loc = menu.toLoc

  def render = {

    val params = FightEdit.loc.currentValue.get
    val f: Fight[_, _] = FightHelper.dao(params.phaseType).findByKey(params.fightId).get
    val totalScore = f.currentScore

    val df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    def process() {
      f.save()
      S.redirectTo("/tournaments/view/" + f.phase.foreign.get.tournament.is)
    }

    def addScoreLine() {
      f.addScore
      f.save()
      Reload
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