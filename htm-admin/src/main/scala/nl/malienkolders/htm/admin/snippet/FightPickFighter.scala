package nl.malienkolders.htm.admin.snippet

import net.liftweb._
import common._
import http._
import sitemap._
import Loc._
import util.Helpers._
import nl.malienkolders.htm.lib.model._
import java.text.SimpleDateFormat
import net.liftweb.http.js.JsCmds._
import java.util.Date
import scala.xml.Text
import nl.malienkolders.htm.lib.rulesets._

object FightPickFighter {

  val menu = (Menu.param[ParamInfo]("Pick Fighter", "Pick Fighter", s => Full(ParamInfo(s)),
    pi => pi.param) / "fights" / "pick" >> Hidden)
  lazy val loc = menu.toLoc

  def render = {

    val param = FightPickFighter.loc.currentValue.get.param
    val id = param.dropRight(1).toLong
    val side = param.takeRight(1)
    val current: Fight[_, _] = EliminationFight.findByKey(id).get
    val t = current.phase.foreign.get.tournament.foreign.get

    def fighter = side match {
      case "A" => current.fighterAFuture
      case _ => current.fighterBFuture
    }

    def redirect = RedirectTo(s"/tournaments/view/${t.identifier.get}#fight${current.id.get}") & Reload

    def pickFighter(p: Participant) = {
      fighter(SpecificFighter(Some(p)).format)
      current.save()
      redirect
    }

    def pickPoolFighter(p: Pool, ranking: Int) = {
      fighter(PoolFighter(p, ranking).format)
      current.save()
      redirect
    }

    def pickFightWinner(f: EliminationFight) = {
      fighter(Winner(f).format)
      current.save()
      redirect
    }

    def pickFightLoser(f: EliminationFight) = {
      fighter(Loser(f).format)
      current.save()
      redirect
    }

    def average(s: Scores): Scores = {
      val denominator = s.fields(0).value().max(1)
      GenericScores(s.numberOfFights, s.fields.map(s =>
        s.copy(value = () => s.value().toString.toDouble / denominator)))
    }

    def globalRanking = {
      implicit val random = scala.util.Random
      val r = t.poolPhase.rulesetImpl
      val rows = r.ranking(t.poolPhase).flatMap {
        case (pool, poolParticipants) =>
          poolParticipants.map { case (participant, scores) => (pool, participant, scores, average(scores)) }
      }
      rows.sortBy(_._4)
    }

    "#pick-participant" #> (
      "thead" #> (".score" #> t.poolPhase.rulesetImpl.emptyScore.header) &
      ".participant" #> globalRanking.map {
        case (pool, participant, scores, average) =>
          ".number *" #> participant.subscription(t).get.fighterNumber.get &
            ".name *" #> SHtml.a(() => pickFighter(participant), Text(participant.name.get)) &
            ".pool *" #> pool.poolName &
            ".score" #> (("* *" #> scores.numberOfFights) :: (scores.fields.zip(average.fields).map { case (s, avg) => "* *" #> <span><span class="absolute-value">{ s.value().toString }</span>/<span class="average-value">{ avg.value().toString }</span></span> }).toList)

      }) &
      ".pool" #> t.poolPhase.pools.map(p =>
        ".name *" #> s"Pool ${p.poolName}" &
          ".number" #> (1 to 8).map(i =>
            "a" #> SHtml.a(() => pickPoolFighter(p, i), Text(i.toString)))) &
      ".fight" #> t.eliminationPhase.fights.filterNot(_.id.is == current.id.is).map(f =>
        ".name *" #> f.name.get &
          ".winner *" #> SHtml.a(() => pickFightWinner(f), Text("Winner")) &
          ".loser *" #> SHtml.a(() => pickFightLoser(f), Text("Loser")))
  }

}