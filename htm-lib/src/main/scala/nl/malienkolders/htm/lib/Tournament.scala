package nl.malienkolders.htm.lib

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

abstract class Tournament {

  type Scores <: nl.malienkolders.htm.lib.Scores

  def id: String

  def planning(round: Round): List[Pool]

  def ranking(p: Pool): List[(Participant, Scores)]

  def ranking(r: Round): List[(Pool, List[(Participant, Scores)])]

  def register = Tournament.registerRuleset(this)

  def compare(s1: Scores, s2: Scores)(implicit random: scala.util.Random): Boolean

  def emptyScore: Scores

  def renderRankedFighter(rank: Int, p: Participant) =
    ".ranking *" #> rank &
      ".name *" #> p.name &
      ".club [title]" #> p.club &
      ".club *" #> p.clubCode

}

object Tournament {
  private var _rulesets = List[Tournament]()

  def registerRuleset(t: Tournament): Unit = {
    _rulesets = _rulesets :+ t
  }

  def ruleset(id: String): Tournament =
    rulesets.find(_.id == id).getOrElse(rulesets.head);

  def rulesets = _rulesets
}