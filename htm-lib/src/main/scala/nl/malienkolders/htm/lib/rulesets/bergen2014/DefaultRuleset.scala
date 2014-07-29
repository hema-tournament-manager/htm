package nl.malienkolders.htm.lib.rulesets
package bergen2014

import nl.malienkolders.htm.lib.model._
import nl.malienkolders.htm.lib.util.Helpers._
import net.liftweb.mapper._
import net.liftweb.util.TimeHelpers._
import scala.xml.Elem

case class ParticipantScores(
    initialRanking: Int,
    fights: Int,
    wins: Int,
    losses: Int,
    draws: Int,
    exchangePoints: Int) extends Scores {
  def group = if (fights > 0) exchangePoints else -10 + initialRanking

  def none(caption: String) = <span>{ caption }</span>
  def asc(caption: String) = <span><span>{ caption }</span><small class="glyphicon glyphicon-sort-by-attributes"></small></span>
  def desc(caption: String) = <span><span>{ caption }</span><small class="glyphicon glyphicon-sort-by-attributes-alt"></small></span>

  val numberOfFights = fights

  val fields: List[ScoreField] = List(
    ScoreField("Wins", desc("W"), HighestFirst, wins),
    ScoreField("Losses", asc("L"), LowestFirst, losses),
    ScoreField("Draws", asc("D"), LowestFirst, draws))
}

abstract class BergenOpenRuleset extends Ruleset {
  type Scores = ParticipantScores

  val emptyScore = ParticipantScores(0, 0, 0, 0, 0, 0)

  def compare(s1: ParticipantScores, s2: ParticipantScores)(implicit random: scala.util.Random) = {
    (s1, s2) match {
      case (ParticipantScores(i1, f1, w1, l1, d1, p1), ParticipantScores(i2, f2, w2, l2, d2, p2)) =>

        if (f1 == 0 && f2 == 0) {
          // if both haven't fought yet: order by initial ranking
          i1 > i2
        } else if (f1.min(f2) == 0) {
          // put fighters who have fought before those who haven't
          f2 == 0
        } else {
          // rank according to Bergen Open 2014 rules
          if (w1 != w2) {
            // a. most wins
            w1 > w2
          } else if (l1 != l2) {
            // b. fewest losses
            l1 < l2
          } else if (d1 != d2) {
            // d. fewest draws
            d1 < d2
          } else {
            // z. randomly
            random.nextBoolean
          }
        }
    }
  }

  def roundRobinPairing(nrOfPeople: Int, iteration: Int): List[(Int, Int)] = {
    val pin = 1
    val (topRow: List[Int], bottomRow: List[Int]) = rotate(topRowForCount(nrOfPeople), bottomRowForCount(nrOfPeople), iteration)
    val result = (pin +: topRow).zip(bottomRow.reverse)
    if (nrOfPeople == 5 && (iteration == 3 || iteration == 4))
      result.reverse
    else
      result
  }

  def topRowForCount(nrOfPeople: Int): List[Int] = (2 to ((nrOfPeople + 1) / 2)).toList

  def bottomRowForCount(nrOfPeople: Int): List[Int] = ((((nrOfPeople + 1) / 2) + 1) to nrOfPeople).toList ++ (if (nrOfPeople.isOdd) List(-1) else List())

  def rotate(topRow: List[Int], bottomRow: List[Int], iterations: Int): (List[Int], List[Int]) = iterations match {
    case 0 => (topRow, bottomRow)
    case _ => rotate(
      bottomRow.takeRight(1) ++ topRow.dropRight(1),
      topRow.takeRight(1) ++ bottomRow.dropRight(1),
      iterations - 1)
  }

  def planning(pool: Pool): List[PoolFight] = {
    val maxNumberOfRounds = pool.participants.size - (if (pool.participants.size.isEven) 1 else 0)

    // don't generate fights after all fighters have faced each other
    // with 4 fighters everyone has to fight 3 times, so you need 3 rounds
    // with 5 fighters everyone has to fight 4 times, but every round one person cannot fight, so you need 5 rounds
    val rawPairings = (for (i <- 0 to (maxNumberOfRounds - 1)) yield { roundRobinPairing(pool.participants.size, i) }).flatten
    val pairings = rawPairings.filter(p => p._1 != -1 && p._2 != -1)
    pairings.zipWithIndex.map {
      case ((a, b), i) =>
        val subA = pool.participants(a - 1).subscription(pool.tournament)
        val subB = pool.participants(b - 1).subscription(pool.tournament)

        PoolFight.create
          .fighterAFuture(SpecificFighter(Some(pool.participants(a - 1))).format)
          .fighterBFuture(SpecificFighter(Some(pool.participants(b - 1))).format)
          .order(i + 1)
          .cancelled(List(subA, subB).flatten.exists(_.droppedOut.is))
    }.toList
  }

  def ranking(p: Pool): List[(Participant, ParticipantScores)] = {
    // seed the Random with the pool id, so the random ranking is always the same for this pool
    implicit val random = new scala.util.Random(p.id.is)
    val pts = p.participants.toList
    val r = p.phase.obj.get
    val t = r.tournament.obj.get
    val fs = p.fights.filter(_.finished_?)
    val result = pts.map(pt => (pt -> fs.foldLeft(ParticipantScores(pt.initialRanking(t), 0, 0, 0, 0, 0)) {
      case (ps @ ParticipantScores(i, c, w, l, d, p), f) =>
        if (!f.cancelled.is && f.inFight_?(pt)) {
          f.currentScore match {
            case TotalScore(a, _, b, _, _, _, _, _) if a == b && f.fighterA.participant.get.id.is == pt.id.is =>
              ParticipantScores(i, c + 1, w, l, d + 1, p + a)
            case TotalScore(a, _, b, _, _, _, _, _) if a == b && f.fighterB.participant.get.id.is == pt.id.is =>
              ParticipantScores(i, c + 1, w, l, d + 1, p + b)
            case TotalScore(a, _, b, _, _, _, _, _) if a > b && f.fighterA.participant.get.id.is == pt.id.is =>
              ParticipantScores(i, c + 1, w + 1, l, d, p + a)
            case TotalScore(a, _, b, _, _, _, _, _) if a > b && f.fighterB.participant.get.id.is == pt.id.is =>
              ParticipantScores(i, c + 1, w, l + 1, d, p + b)
            case TotalScore(a, _, b, _, _, _, _, _) if a < b && f.fighterA.participant.get.id.is == pt.id.is =>
              ParticipantScores(i, c + 1, w, l + 1, d, p + a)
            case TotalScore(a, _, b, _, _, _, _, _) if a < b && f.fighterB.participant.get.id.is == pt.id.is =>
              ParticipantScores(i, c + 1, w + 1, l, d, p + b)
            case _ => ParticipantScores(i, c, w, l, d, p)
          }
        } else {
          ps
        }
    })).sortWith((pt1, pt2) => compare(pt1._2, pt2._2))
    def droppedOut(part: Participant) = part.subscription(p.tournament).map(_.droppedOut.is).getOrElse(true)
    // Always put dropped out fighters last
    result.filter(r => !droppedOut(r._1)) ++ result.filter(r => droppedOut(r._1))
  }

  val possiblePoints = List(0, 1, 2, 3)

  def fightProperties = FightProperties(
    timeLimit = 4 minutes,
    breakAt = 2 minutes,
    breakDuration = 30 seconds,
    timeBetweenFights = 30 seconds,
    exchangeLimit = 0,
    doubleHitLimit = 0,
    pointLimit = 0)
}

object BergenOpenRuleset {
  def registerAll(): Unit = {

  }
}

trait PoolPhaseRuleset extends BergenOpenRuleset

trait EliminationRuleset extends BergenOpenRuleset

trait FinalsRuleset extends EliminationRuleset

trait DefaultRuleset extends BergenOpenRuleset

object DefaultPoolPhaseRuleset extends DefaultRuleset with PoolPhaseRuleset {
  val id = "bergen-2014-default-pools"
}

object DefaultEliminationRuleset extends DefaultRuleset with EliminationRuleset {
  val id = "bergen-2014-default-elimination"
}

object DefaultFinalsRuleset extends DefaultRuleset with FinalsRuleset {
  val id = "bergen-2014-default-finals"
}
