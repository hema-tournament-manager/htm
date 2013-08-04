package nl.malienkolders.htm.admin.snippet

import net.liftweb._
import http._
import mapper._
import util.Helpers._
import scala.xml.NodeSeq
import nl.malienkolders.htm.lib.model.Tournament

class TournamentList {

  def render = {
    "#tournament" #> Tournament.findAll(OrderBy(Tournament.id, Ascending)).map(t =>
      "#tournamentName" #> <a href={ "view/" + t.identifier.is }>{ t.name }</a> &
        "#tournamentEdit [href]" #> ("edit/" + t.identifier.is) &
        "#tournamentIdentifier" #> t.identifier.is &
        "#tournamentParticipants" #> t.participants.size &
        "#tournamentRounds" #> t.rounds.size &
        "#tournamentFights" #> t.rounds.flatMap(_.pools.flatMap(_.fights.map(f => 1))).size)
  }

}