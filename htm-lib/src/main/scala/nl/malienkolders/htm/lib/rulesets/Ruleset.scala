package nl.malienkolders.htm.lib.rulesets

import nl.malienkolders.htm.lib.model._
import net.liftweb._
import net.liftweb.http._
import net.liftweb.mapper._
import net.liftweb.util.Helpers._
import net.liftweb.util.StringPromotable.intToStrPromo
import scala.xml.Elem
import scala.xml.NodeBuffer
import net.liftweb.util.CssSel

abstract class Scores {
  def fields: Seq[(String, () => AnyVal)]

  implicit def val2lazyVal(i: AnyVal): () => AnyVal =
    () => i

  def header: Seq[Elem] = fields.map {
    case (name, _) => <th>{ name }</th>
  }

  def row: Seq[Elem] = fields.map {
    case (name, value) => <td title={ name }>{ value() }</td>
  }
}

abstract class Ruleset {

  type Scores <: nl.malienkolders.htm.lib.rulesets.Scores

  def id: String

  def planning(phase: PoolPhase): List[Pool]
  
  def ranking(p: Pool): List[(Participant, Scores)]

  def ranking(phase: PoolPhase): List[(Pool, List[(Participant, Scores)])] = 
    phase.pools.map { p =>
      (p, ranking(p))
    }.toList

  def register(default: Boolean = false) = Ruleset.registerRuleset(this, default)

  def compare(s1: Scores, s2: Scores)(implicit random: scala.util.Random): Boolean

  def emptyScore: Scores

  def possiblePoints: List[Int]

  def renderRankedFighter(rank: Int, p: Participant, t: Tournament) =
    ".ranking *" #> rank &
      ".name *" #> p.name &
      ".club [title]" #> p.club &
      ".club *" #> p.clubCode &
      ".pool *" #> findPool(t, p).map(_.poolName).getOrElse("?")

  def findPool(tournament: Tournament, p: Participant) =
    tournament.poolPhase.pools.find(_.participants.contains(p))

}

object Ruleset {
  private var _rulesets = Map[String, Ruleset]()

  private var _defaultRuleset: Ruleset = _

  def registerRuleset(t: Ruleset, default: Boolean = false): Unit = {
    _rulesets = _rulesets + (t.id -> t)
    if (default) {
      _defaultRuleset = t
    }
  }

  def apply() = _defaultRuleset

  def apply(id: String) = rulesets.get(id).get

  def ruleset(id: String): Option[Ruleset] = rulesets.get(id)

  def rulesets = _rulesets
}