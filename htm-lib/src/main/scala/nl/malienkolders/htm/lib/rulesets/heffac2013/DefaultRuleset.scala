package nl.malienkolders.htm.lib.rulesets
package heffac2013

import scala.collection.immutable.List
import _root_.nl.malienkolders.htm.lib.model._
import nl.malienkolders.htm.lib.util.Helpers._
import net.liftweb._
import net.liftweb.http._
import net.liftweb.mapper._
import net.liftweb.util.Helpers._
import net.liftweb.util.StringPromotable.intToStrPromo

case class ParticipantScores(
    initialRanking: Int,
    fights: Int,
    wins: Int,
    ties: Int,
    losses: Int,
    cleanHitsReceived: Int,
    cleanHitsDealt: Int,
    afterblowsReceived: Int,
    afterblowsDealt: Int,
    doubleHits: Int) extends Scores {
  def points = wins * 3 + ties * 1
  def group = if (fights > 0) points else -10 + initialRanking
  def hitsReceived = cleanHitsReceived + afterblowsReceived + afterblowsDealt + doubleHits
  def firstHits = cleanHitsDealt + afterblowsDealt

  val fields: List[(String, () => AnyVal)] = List(
    "nr of fights" -> fights,
    "points" -> points,
    "clean hits received" -> cleanHitsReceived,
    "double hits" -> doubleHits,
    "clean hits dealt" -> cleanHitsDealt)
}

object DefaultRuleset extends Ruleset {

  val id = "heffac-2013-default"

  type Scores = ParticipantScores

  val possiblePoints = List(0, 1, 2, 3)

  val emptyScore = ParticipantScores(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)

  def compare(s1: ParticipantScores, s2: ParticipantScores)(implicit random: scala.util.Random) = {
    (s1, s2) match {
      case (ParticipantScores(i1, f1, w1, t1, l1, cr1, c1, _, _, d1), ParticipantScores(i2, f2, w2, t2, l2, cr2, c2, _, _, d2)) =>
        if (f1 == 0 && f2 == 0) {
          // if both haven't fought yet: order by initial ranking
          i1 > i2
        } else if (f1.min(f2) == 0) {
          // put fighters who have fought before those who haven't
          f2 == 0
        } else {
          // rank according to HEFFAC rules
          if (s1.points != s2.points) {
            // a. highest score
            s1.points > s2.points
          } else if (cr1 != cr2) {
            // b. fewest clean hits received
            cr1 < cr2
          } else if (d1 != d2) {
            // d. fewest doubles
            d1 < d2
          } else if (c1 != c2) {
            // f. most clean hits dealt
            c1 > c2
          } else {
            // i. randomly
            random.nextBoolean
          }
        }
    }
  }

  def roundRobinPairing(nrOfPeople: Int, iteration: Int): List[(Int, Int)] = {
    val pin = 1
    val (topRow: List[Int], bottomRow: List[Int]) = rotate(topRowForCount(nrOfPeople), bottomRowForCount(nrOfPeople), iteration)
    (pin +: topRow).zip(bottomRow.reverse)
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
        PoolFight.create
          .fighterAFuture(SpecificFighter(Some(pool.participants(a - 1))).format)
          .fighterBFuture(SpecificFighter(Some(pool.participants(b - 1))).format)
          .order(i + 1)
    }.toList
  }

  def ranking(p: Pool): List[(Participant, ParticipantScores)] = {
    // seed the Random with the pool id, so the random ranking is always the same for this pool
    implicit val random = new scala.util.Random(p.id.is)
    val pts = p.participants.toList
    val r = p.phase.obj.get
    val t = r.tournament.obj.get
    val fs = p.fights.filter(_.finished_?)
    pts.map(pt => (pt -> fs.foldLeft(ParticipantScores(pt.initialRanking(t), 0, 0, 0, 0, 0, 0, 0, 0, 0)) {
      case (ps @ ParticipantScores(i, c, w, t, l, hR, hD, aR, aD, d), f) =>
        if (f.inFight_?(pt)) {
          f.currentScore match {
            case TotalScore(a, aafter, b, bafter, double, _) if double >= 3 && f.fighterA.participant.get.id.is == pt.id.is =>
              ParticipantScores(i, c + 1, w, t, l, hR + b, hD + a, aR + aafter, aD + bafter, d + double)
            case TotalScore(a, aafter, b, bafter, double, _) if double >= 3 && f.fighterB.participant.get.id.is == pt.id.is =>
              ParticipantScores(i, c + 1, w, t, l, hR + a, hD + b, aR + bafter, aD + aafter, d + double)
            case TotalScore(a, aafter, b, bafter, double, _) if a == b && f.fighterA.participant.get.id.is == pt.id.is =>
              ParticipantScores(i, c + 1, w, t + 1, l, hR + b, hD + a, aR + aafter, aD + bafter, d + double)
            case TotalScore(a, aafter, b, bafter, double, _) if a == b && f.fighterB.participant.get.id.is == pt.id.is =>
              ParticipantScores(i, c + 1, w, t + 1, l, hR + a, hD + b, aR + bafter, aD + aafter, d + double)
            case TotalScore(a, aafter, b, bafter, double, _) if a > b && f.fighterA.participant.get.id.is == pt.id.is =>
              ParticipantScores(i, c + 1, w + 1, t, l, hR + b, hD + a, aR + aafter, aD + bafter, d + double)
            case TotalScore(a, aafter, b, bafter, double, _) if a > b && f.fighterB.participant.get.id.is == pt.id.is =>
              ParticipantScores(i, c + 1, w, t, l + 1, hR + a, hD + b, aR + bafter, aD + aafter, d + double)
            case TotalScore(a, aafter, b, bafter, double, _) if a < b && f.fighterA.participant.get.id.is == pt.id.is =>
              ParticipantScores(i, c + 1, w, t, l + 1, hR + b, hD + a, aR + aafter, aD + bafter, d + double)
            case TotalScore(a, aafter, b, bafter, double, _) if a < b && f.fighterB.participant.get.id.is == pt.id.is =>
              ParticipantScores(i, c + 1, w + 1, t, l, hR + a, hD + b, aR + bafter, aD + aafter, d + double)
            case _ => ParticipantScores(i, c, w, t, l, hR, hD, aR, aD, d)
          }
        } else {
          ps
        }
    })).sortWith((pt1, pt2) => compare(pt1._2, pt2._2))
  }

  val fightProperties = FightProperties(
    timeLimit = 3 minutes,
    breakAt = 0,
    breakDuration = 0,
    timeBetweenFights = 2 minute,
    exchangeLimit = 10)
}