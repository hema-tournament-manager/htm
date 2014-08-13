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
import java.util.TimeZone

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
    val newScore: Score[_, _] = f.createScore.asInstanceOf[Score[_, _]]
    val totalScore = f.currentScore

    val df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    df.setTimeZone(TimeZone.getTimeZone("UTC"))

    def process() {
      f.save()
      Reload
    }

    def deleteFight() {
      val tournamentIdentifier = f.tournament.identifier.get
      f.delete_!
      S.redirectTo("/tournaments/view/" + tournamentIdentifier)
    }

    def addScoreLine() {
      f.addScore(newScore)
      f.save()
      Reload
    }

    def renderFighter(fighter: Fighter) = (fighter match {
      case UnknownFighter(_) =>
        ".name *" #> "Unknown fighter" &
          ".club [title]" #> "N/A" &
          ".club *" #> "N/A"
      case knownFighter => knownFighter.participant match {
        case Some(pt) =>
          ".name *" #> fighter.participant.get.name.is &
            ".club [title]" #> fighter.participant.get.club.is &
            ".club *" #> fighter.participant.get.clubCode.is
        case None =>
          ".name *" #> knownFighter.toString &
            ".club [title]" #> "N/A" &
            ".club *" #> "N/A"
      }
    })

    val dateFormatStr = "yy-mm-dd";
    val timeFormatStr = "HH:mm:ss";

    val datetimepickerInitStr = s"datetimepicker({ dateFormat: '$dateFormatStr', timeFormat: '$timeFormatStr' });";

    val deleteMapping = params.phaseType match {
      case FreeStyleType.code if !f.finished_? =>
        "#doDelete" #> SHtml.onSubmitUnit(deleteFight)
      case _ =>
        "#doDelete" #> Nil
    }

    S.appendJs(Run("$('#timeStop')." + datetimepickerInitStr) & Run("$('#timeStart')." + datetimepickerInitStr))
    ".red" #> renderFighter(f.fighterA) &
      ".blue" #> renderFighter(f.fighterB) &
      "#scoreRed" #> totalScore.red &
      "#scoreBlue" #> totalScore.blue &
      "#doAdd" #> SHtml.onSubmitUnit(addScoreLine) &
      "name=timeStart" #> SHtml.text(df.format(new Date(f.timeStart.get)), s => f.timeStart(df.parse(s).getTime()), "id" -> "timeStart", "class" -> "hasDatePicker") &
      "name=timeStop" #> SHtml.text(df.format(new Date(f.timeStop.get)), s => f.timeStop(df.parse(s).getTime()), "id" -> "timeStop", "class" -> "hasDatePicker") &
      ".score" #> f.mapScores(score =>
        ".pointsRed" #> score.pointsRed.get &
          ".pointsBlue" #> score.pointsBlue.get &
          ".cleanHitsRed" #> score.cleanHitsRed.get &
          ".cleanHitsBlue" #> score.cleanHitsBlue.get &
          ".afterblowsRed" #> score.afterblowsRed.get &
          ".afterblowsBlue" #> score.afterblowsBlue.get &
          ".doubles" #> score.doubles.get &
          ".exchanges" #> score.exchanges.get &
          ".scoreType" #> score.scoreType.get) &
      ".newScore" #> (
        "name=pointsRed" #> newScore.pointsRed.toForm &
        "name=pointsBlue" #> newScore.pointsBlue.toForm &
        "name=cleanHitsRed" #> newScore.cleanHitsRed.toForm &
        "name=cleanHitsBlue" #> newScore.cleanHitsBlue.toForm &
        "name=afterblowsRed" #> newScore.afterblowsRed.toForm &
        "name=afterblowsBlue" #> newScore.afterblowsBlue.toForm &
        "name=doubles" #> newScore.doubles.toForm &
        "name=exchanges" #> newScore.exchanges.toForm &
        "name=scoreType" #> newScore.scoreType.toForm) &
        "#doEdit" #> SHtml.onSubmitUnit(process) &
        deleteMapping

  }

}