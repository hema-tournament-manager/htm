package nl.malienkolders.htm.lib

import nl.malienkolders.htm.lib.model._
import net.liftweb.util.CssSel

abstract class Tournament {

  type Scores

  def id: String

  def planning(round: Round): List[Pool]

  def ranking(p: Pool): List[(Participant, Scores)]

  def ranking(r: Round): List[(Pool, List[(Participant, Scores)])]

  def renderRankedFighter(rank: Int, p: Participant, s: Scores): CssSel

  def register = Tournament.registerRuleset(this)
  
}

object Tournament {
  private var _rulesets = List[Tournament]()

  def registerRuleset(t: Tournament): Unit = {
    _rulesets = _rulesets :+ t
  }

  def ruleset(id: String): Option[Tournament] =
    rulesets.find(_.id == id)

  def rulesets = _rulesets
}