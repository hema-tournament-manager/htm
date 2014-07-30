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
import scala.util.Random

case class ScoreField(name: String, header: Elem, sort: ScoreSort, value: () => Double)

sealed trait ScoreSort {
  def compare(a: Double, b: Double): Int
}
case object LowestFirst extends ScoreSort {
  def compare(a: Double, b: Double): Int = a.compare(b)
}
case object HighestFirst extends ScoreSort {
  def compare(a: Double, b: Double): Int = LowestFirst.compare(b, a)
}

abstract class Scores extends Comparable[Scores] {
  def numberOfFights: Int

  def fields: Seq[ScoreField]

  implicit def int2lazyDouble(i: Int): () => Double =
    () => i.toDouble

  def header: Seq[Elem] = <th title="Number of fights">#</th> :: fields.toList.map {
    case ScoreField(name, header, _, _) => <th title={ name }>{ header }</th>
  }

  def row: Seq[Elem] = <td title="Number of fights">{ numberOfFights }</td> :: fields.toList.map {
    case ScoreField(name, _, _, value) => <td title={ name }>{ value() }</td>
  }

  private def findFirstDifference(fields: List[(ScoreField, ScoreField)]): Int = fields match {
    case head :: tail => head match {
      case (a, b) if a == b => findFirstDifference(tail)
      case (a, b) => a.sort.compare(a.value(), b.value())
    }
    case Nil => if (Random.nextBoolean) 1 else -1
  }

  override def compareTo(other: Scores): Int = findFirstDifference(fields.toList.zip(other.fields.toList))
}

case class GenericScores(numberOfFights: Int, fields: Seq[ScoreField]) extends Scores

sealed abstract class Side(val serialized: String)
case object LeftSide extends Side("left")
case object RightSide extends Side("right")
case object CenterSide extends Side("center")

case class Scoring(points: InfluencedPoints, effect: Effect)

sealed abstract class InfluencedPoints(val serialized: String)
case object ExchangePoints extends InfluencedPoints("x")
case object PointsLeft extends InfluencedPoints("a")
case object PointsRight extends InfluencedPoints("b")
case object DoubleHits extends InfluencedPoints("d")
case object CleanHitsLeft extends InfluencedPoints("cA")
case object CleanHitsRight extends InfluencedPoints("cB")
case object AfterblowsLeft extends InfluencedPoints("aA")
case object AfterblowsRight extends InfluencedPoints("aB")

sealed abstract class Effect(val serialized: String)
case object PlusOne extends Effect("inc")
case class Pick(points: List[Int]) extends Effect("[" + points.mkString(",") + "]")

case class Hit(name: String, scoreType: String, side: Side, scorings: List[Scoring])

case class FightProperties(timeLimit: Long, breakAt: Long, breakDuration: Long, timeBetweenFights: Long, exchangeLimit: Int, doubleHitLimit: Int, pointLimit: Int, possibleHits: List[Hit])

trait Ruleset {

  type Scores <: nl.malienkolders.htm.lib.rulesets.Scores

  def id: String

  def planning(pool: Pool): List[PoolFight]

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

  def fightProperties: FightProperties
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

  def apply(id: String) = rulesets.get(id).getOrElse(_defaultRuleset)

  def ruleset(id: String): Option[Ruleset] = rulesets.get(id)

  def rulesets = _rulesets
}