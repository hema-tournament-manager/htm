package nl.malienkolders.htm.admin.snippet

import net.liftweb._
import common._
import http._
import sitemap._
import util.Helpers._
import js._
import JsCmds._
import mapper._
import nl.malienkolders.htm.lib.model._
import nl.malienkolders.htm.lib.{ Tournament => Rulesets }
import nl.malienkolders.htm.admin.lib.TournamentUtils._
import scala.util.Random

object TournamentAdvance {
  val menu = Menu.param[ParamInfo]("Advance Participants", "Advance Participants", s => Full(ParamInfo(s)),
    pi => pi.param) / "tournaments" / "advance"
  lazy val loc = menu.toLoc

  def render = {

    val cr = Round.findByKey(TournamentAdvance.loc.currentValue.map(_.param).get.toLong).get
    val t = cr.tournament.obj.get
    val pr = cr.previousRound.get
    val ruleset = Rulesets.ruleset(cr.ruleset.get)

    var selected: List[Participant] = List()

    implicit val random = new scala.util.Random(cr.id.is)

    val allParticipants = ruleset.ranking(pr).foldLeft(List[(Participant, _ <: ruleset.Scores)]())(_ ++ _._2)

    "thead" #> (".scores" #> ruleset.emptyScore.header) &
      ".participant" #> allParticipants.sortWith((a, b) => ruleset.compare(a._2, b._2)).zipWithIndex.map {
        case ((p, s), i) =>
          ".selected *" #> SHtml.checkbox(false, b => if (b) selected = p :: selected) &
            ruleset.renderRankedFighter(i + 1, p) &
            ".scores" #> s.row
      } &
      ".prevRound" #> pr.name &
      ".nextRound" #> cr.name &
      "#ok" #> SHtml.onSubmitUnit { () =>
        advance(cr, Selected(selected))
        S.redirectTo("/tournaments/view/" + cr.tournament.is)
      } &
      "#cancel" #> SHtml.onSubmitUnit(() => S.redirectTo("/tournaments/view/" + cr.tournament.is))

  }

}