package nl.malienkolders.htm.lib.rulesets
package swordfish2013
package rapier

import nl.malienkolders.htm.lib.util.Helpers._
import nl.malienkolders.htm.lib.model._
import net.liftweb.mapper._
import net.liftweb.util.TimeHelpers._
import scala.xml.Elem
import scala.xml.Text

case class ParticipantScores(
    initialRanking: Int,
    fights: Int,
    wins: Int,
    ties: Int,
    losses: Int,
    lossesByDoubles: Int,
    cleanHitsReceived: Int,
    cleanHitsDealt: Int,
    afterblowsReceived: Int,
    afterblowsDealt: Int,
    doubleHits: Int,
    exchangePoints: Int) extends Scores {

  def hitsReceived = cleanHitsReceived + afterblowsReceived + afterblowsDealt + doubleHits
  def firstHits = cleanHitsDealt + afterblowsDealt
  def doubleHitsAverage = if (fights == 0) 0 else doubleHits.toDouble / fights

  def points = wins * 3 + ties * 1

  val fields: List[((String, Elem), () => AnyVal)] = List[(String, () => AnyVal)](
    "nr of fights" -> fights,
    "points" -> points,
    "wins" -> wins,
    "ties" -> ties,
    "losses" -> losses,
    "points scored" -> exchangePoints,
    "fights lost by doubles" -> lossesByDoubles,
    "double hits" -> doubleHits,
    "afterblows received" -> afterblowsReceived,
    "average double hits" -> doubleHitsAverage).map {
      case (n, v) =>
        (n, Text(n).asInstanceOf[Elem]) -> v
    }
}

object RapierRuleset extends Ruleset {
  type Scores = ParticipantScores

  val emptyScore = ParticipantScores(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)

  val id = "swordfish-2013-rapier"
  val possiblePoints = List(0, 1, 2, 3)

  def compare(s1: ParticipantScores, s2: ParticipantScores)(implicit random: scala.util.Random) = {
    (s1, s2) match {
      case (ParticipantScores(i1, f1, w1, _, _, lbd1, _, _, ar1, _, d1, p1), ParticipantScores(i2, f2, w2, _, _, lbd2, _, _, ar2, _, d2, p2)) =>
        val effectivePoints1 = p1 - lbd1
        val effectivePoints2 = p2 - lbd2
        if (f1 == 0 && f2 == 0) {
          // if both haven't fought yet: order by initial ranking
          i1 > i2
        } else if (f1.min(f2) == 0) {
          // put fighters who have fought before those who haven't
          f2 == 0
        } else {
          // rank according to Swordfish rules
          if (s1.points != s2.points) {
            // 0. most points
            s1.points > s2.points
          } else if (w1 != w2) {
            // a. most wins
            w1 > w2
          } else if (effectivePoints1 != effectivePoints2) {
            // b. most points scored
            effectivePoints1 > effectivePoints2
          } else if (d1 != d2) {
            // d. fewest doubles
            d1 < d2
          } else if (ar1 != ar2) {
            // f. fewest afterblows received
            ar1 > ar2
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
        PoolFight.create
          .fighterAFuture(pool.participants(a - 1))
          .fighterBFuture(pool.participants(b - 1))
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
    pts.map(pt => (pt -> fs.foldLeft(ParticipantScores(pt.initialRanking(t), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)) {
      case (ps @ ParticipantScores(i, c, w, t, l, lbd, hR, hD, aR, aD, d, p), f) =>
        if (f.inFight_?(pt)) {
          f.currentScore match {
            // lose all other points on three double hits
            case TotalScore(_, _, _, _, double, _) if (double >= 3) =>
              ParticipantScores(i, c + 1, w, t, l + 1, lbd + 1, hR, hD, aR, aD, d + double, p)
            // In the case of a tie, the score will be the fighter’s score minus doubles, with a minimum of 0 points.
            case TotalScore(a, aafter, b, bafter, double, _) if a == b && f.fighterA.participant.get.id.is == pt.id.is =>
              ParticipantScores(i, c + 1, w, t + 1, l, lbd, hR + b, hD + a, aR + aafter, aD + bafter, d + double, p + (a.min(6) - double).max(0))
            case TotalScore(a, aafter, b, bafter, double, _) if a == b && f.fighterB.participant.get.id.is == pt.id.is =>
              ParticipantScores(i, c + 1, w, t + 1, l, lbd, hR + a, hD + b, aR + bafter, aD + aafter, d + double, p + (b.min(6) - double).max(0))
            // If the winner has more than 6 points, the winner’s score will first be reduced to 6
            // The loser’s points will be then deducted from the winner’s 
            // Double hits are removed from the remaining winning points
            case TotalScore(a, aafter, b, bafter, double, _) if a > b && f.fighterA.participant.get.id.is == pt.id.is =>
              ParticipantScores(i, c + 1, w + 1, t, l, lbd, hR + b, hD + a, aR + aafter, aD + bafter, d + double, p + calculateFightPoints(a, b, double))
            // The loser receives no points
            case TotalScore(a, aafter, b, bafter, double, _) if a > b && f.fighterB.participant.get.id.is == pt.id.is =>
              ParticipantScores(i, c + 1, w, t, l + 1, lbd, hR + a, hD + b, aR + bafter, aD + aafter, d + double, p)
            // The loser receives no points
            case TotalScore(a, aafter, b, bafter, double, _) if a < b && f.fighterA.participant.get.id.is == pt.id.is =>
              ParticipantScores(i, c + 1, w, t, l + 1, lbd, hR + b, hD + a, aR + aafter, aD + bafter, d + double, p)
            // If the winner has more than 6 points, the winner’s score will first be reduced to 6
            // The loser’s points will be then deducted from the winner’s 
            // Double hits are removed from the remaining winning points
            case TotalScore(a, aafter, b, bafter, double, _) if a < b && f.fighterB.participant.get.id.is == pt.id.is =>
              ParticipantScores(i, c + 1, w + 1, t, l, lbd, hR + a, hD + b, aR + bafter, aD + aafter, d + double, p + calculateFightPoints(b, a, double))
            case _ => ps
          }
        } else {
          ps
        }
    })).sortWith((pt1, pt2) => compare(pt1._2, pt2._2))
  }

  def calculateFightPoints(pointsWinner: Int, pointsLoser: Int, doubles: Int): Int =
    (pointsWinner.min(6) - pointsLoser) - doubles

  val fightProperties = FightProperties(
    timeLimit = 3 minutes,
    breakAt = 0,
    breakDuration = 0,
    timeBetweenFights = 2 minute,
    exchangeLimit = 10,
    doubleHitLimit = 3)
}