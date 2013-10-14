package nl.malienkolders.htm.lib

import nl.malienkolders.htm.lib.model._

object SwissSpecialHitsTournament extends Tournament {

  import swiss.ParticipantScores

  val id = "swiss (special hits)"

  type Scores = ParticipantScores

  def emptyScore = SwissTournament.emptyScore

  def compare(s1: ParticipantScores, s2: ParticipantScores)(implicit random: scala.util.Random) = SwissTournament.compare(true)(s1, s2)(random)

  def ranking(p: Pool): List[(Participant, ParticipantScores)] = SwissTournament.ranking(p)

  def ranking(r: Round): List[(Pool, List[(Participant, ParticipantScores)])] = SwissTournament.ranking(r)

  def planning(round: Round): List[Pool] = SwissTournament.planning(round)

}