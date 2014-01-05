package nl.malienkolders.htm.admin.snippet

import net.liftweb._
import http._
import mapper._
import util.Helpers._
import scala.xml.NodeSeq
import nl.malienkolders.htm.lib.model._

class TournamentList {

  def render = {
    var name = ""
    var identifier = ""
    var label = ""
    var pools = 8

    def process(): Unit = {
      if (Arena.count < 1) {
        Arena.create.name("Arena 1").save()
      }

      val t = Tournament.create.identifier(identifier).name(name).mnemonic(label)
      t.phases ++= List(PoolPhase.create, EliminationPhase.create, EliminationPhase.create)
      t.poolPhase.name("Pool Phase")
      t.poolPhase.pools ++= (for (i <- 1 to pools) yield { Pool.create(t) })
      t.eliminationPhase.name("Elimination Phase")
      t.finalsPhase.name("Finals")
      t.save()

      S.redirectTo("/tournaments/list")
    }

    "#tournament" #> Tournament.findAll(OrderBy(Tournament.id, Ascending)).map(t =>
      "#tournamentName" #> <a href={ "view/" + t.identifier.is }>{ t.name }</a> &
        "#tournamentEdit [href]" #> ("edit/" + t.identifier.is) &
        "#tournamentIdentifier" #> t.identifier.is &
        "#tournamentParticipants" #> t.participants.size &
        "#tournamentPools" #> t.poolPhase.pools.size &
        "#tournamentFights" #> t.phases.map(_.fights.size).foldLeft(0)(_ + _) &
        ".label *" #> t.mnemonic.get) &
      "#newTournament" #> (
        "#name" #> SHtml.text(name, name = _) &
        "#identifier" #> SHtml.text(identifier, identifier = _) &
        "#label" #> SHtml.text(label, label = _) &
        "#pools" #> SHtml.number(pools, pools = _, 1, 128) &
        "#submitNewTournament" #> SHtml.submit("Submit", process))
  }

}