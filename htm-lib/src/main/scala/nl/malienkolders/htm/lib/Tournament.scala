package nl.malienkolders.htm.lib

import nl.malienkolders.htm.lib.model._
import net.liftweb.util.CssSel

abstract class Tournament[Scores] {

  def planning(round: Round): List[Pool]

  def ranking(p: Pool): List[(Participant, Scores)]

  def ranking(r: Round): List[(Pool, List[(Participant, Scores)])]

  def renderRankedFighter(rank: Int, p: Participant, s: Scores): CssSel

}