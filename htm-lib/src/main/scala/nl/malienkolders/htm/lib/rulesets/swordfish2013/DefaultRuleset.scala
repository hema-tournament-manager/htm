package nl.malienkolders.htm.lib.rulesets
package swordfish2013

import nl.malienkolders.htm.lib.model._
import nl.malienkolders.htm.lib.util.Helpers._
import net.liftweb.mapper._

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
    doubleHits: Int) extends Scores {
  def points = wins * 3 + ties * 1 + lossesByDoubles * -1
  def group = if (fights > 0) points else -10 + initialRanking
  def hitsReceived = cleanHitsReceived + afterblowsReceived + afterblowsDealt + doubleHits
  def firstHits = cleanHitsDealt + afterblowsDealt
  def doubleHitsAverage = if (fights == 0) 0 else doubleHits.toDouble / fights

  val fields: List[(String, () => AnyVal)] = List(
    "nr of fights" -> fights,
    "points" -> points,
    "clean hits received" -> cleanHitsReceived,
    "double hits" -> doubleHits,
    "clean hits dealt" -> cleanHitsDealt,
    "average double hits" -> doubleHitsAverage)
}

abstract class SwordfishRuleset extends Ruleset {
  type Scores = ParticipantScores

  val emptyScore = ParticipantScores(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)

  def compare(s1: ParticipantScores, s2: ParticipantScores)(implicit random: scala.util.Random) = {
    (s1, s2) match {
      case (ParticipantScores(i1, f1, w1, t1, l1, lbd1, cr1, c1, _, _, d1), ParticipantScores(i2, f2, w2, t2, l2, lbd2, cr2, c2, _, _, d2)) =>
        if (f1 == 0 && f2 == 0) {
          // if both haven't fought yet: order by initial ranking
          i1 > i2
        } else if (f1.min(f2) == 0) {
          // put fighters who have fought before those who haven't
          f2 == 0
        } else {
          // rank according to Swordfish rules
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

  def planning(round: Round): List[Pool] = {
    val previous = round.previousRounds
    round.pools.map { pool =>
      val pairings = roundRobinPairing(pool.participants.size, previous.size)
      pairings.foreach {
        case (a, b) if a != -1 && b != -1 =>
          pool.addFight(pool.participants(a - 1), pool.participants(b - 1))
        case _ => // do nothing
      }
      pool.save
      pool
    }.toList
  }

  def ranking(p: Pool): List[(Participant, ParticipantScores)] = {
    // seed the Random with the pool id, so the random ranking is always the same for this pool
    implicit val random = new scala.util.Random(p.id.is)
    val pts = p.participants.toList
    val r = p.round.obj.get
    val t = r.tournament.obj.get
    val rs = Round.findAll(By(Round.tournament, t)).filter(_.order.is <= r.order.is)
    val ps = Pool.findAll(ByList(Pool.round, rs.map(_.id.is)))
    val fs = Fight.findAll(ByList(Fight.pool, ps.map(_.id.is))).filter(_.finished_?)
    pts.map(pt => (pt -> fs.foldLeft(ParticipantScores(pt.initialRanking(t), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)) {
      case (ps @ ParticipantScores(i, c, w, t, l, lbd, hR, hD, aR, aD, d), f) =>
        if (f.inFight_?(pt)) {
          f.currentScore match {
            case TotalScore(a, aafter, b, bafter, double, _) if double >= 3 && f.fighterA.is == pt.id.is =>
              ParticipantScores(i, c + 1, w, t, l, lbd + 1, hR + b, hD + a, aR + aafter, aD + bafter, d + double)
            case TotalScore(a, aafter, b, bafter, double, _) if double >= 3 && f.fighterB.is == pt.id.is =>
              ParticipantScores(i, c + 1, w, t, l, lbd + 1, hR + a, hD + b, aR + bafter, aD + aafter, d + double)
            case TotalScore(a, aafter, b, bafter, double, _) if a == b && f.fighterA.is == pt.id.is =>
              ParticipantScores(i, c + 1, w, t + 1, l, lbd, hR + b, hD + a, aR + aafter, aD + bafter, d + double)
            case TotalScore(a, aafter, b, bafter, double, _) if a == b && f.fighterB.is == pt.id.is =>
              ParticipantScores(i, c + 1, w, t + 1, l, lbd, hR + a, hD + b, aR + bafter, aD + aafter, d + double)
            case TotalScore(a, aafter, b, bafter, double, _) if a > b && f.fighterA.is == pt.id.is =>
              ParticipantScores(i, c + 1, w + 1, t, l, lbd, hR + b, hD + a, aR + aafter, aD + bafter, d + double)
            case TotalScore(a, aafter, b, bafter, double, _) if a > b && f.fighterB.is == pt.id.is =>
              ParticipantScores(i, c + 1, w, t, l + 1, lbd, hR + a, hD + b, aR + bafter, aD + aafter, d + double)
            case TotalScore(a, aafter, b, bafter, double, _) if a < b && f.fighterA.is == pt.id.is =>
              ParticipantScores(i, c + 1, w, t, l + 1, lbd, hR + b, hD + a, aR + aafter, aD + bafter, d + double)
            case TotalScore(a, aafter, b, bafter, double, _) if a < b && f.fighterB.is == pt.id.is =>
              ParticipantScores(i, c + 1, w + 1, t, l, lbd, hR + a, hD + b, aR + bafter, aD + aafter, d + double)
            case _ => ParticipantScores(i, c, w, t, l, lbd, hR, hD, aR, aD, d)
          }
        } else {
          ps
        }
    })).sortWith((pt1, pt2) => compare(pt1._2, pt2._2))
  }

  def ranking(r: Round): List[(Pool, List[(Participant, ParticipantScores)])] = {
    r.pools.toList.map { p =>
      (p, ranking(p))
    }
  }
}

object DefaultRuleset extends SwordfishRuleset {
  val id = "swordfish-2013-default"
  val possiblePoints = List(0, 1, 2, 3)
}