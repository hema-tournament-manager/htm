package nl.malienkolders.htm.lib.rulesets
package mexico2014

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
    lossesByDoubles: Int,
    cleanHitsReceived: Int,
    cleanHitsDealt: Int,
    afterblowsReceived: Int,
    afterblowsDealt: Int,
    doubleHits: Int,
    exchangePoints: Int) extends Scores {
  def group = if (fights > 0) exchangePoints else -10 + initialRanking
  def hitsReceived = cleanHitsReceived + afterblowsReceived + afterblowsDealt + doubleHits
  def firstHits = cleanHitsDealt + afterblowsDealt
  def doubleHitsAverage = if (fights == 0) 0 else doubleHits.toDouble / fights

  def none(caption: String) = <span>{ caption }</span>
  def asc(caption: String) = <span><span>{ caption }</span><small class="glyphicon glyphicon-sort-by-attributes"></small></span>
  def desc(caption: String) = <span><span>{ caption }</span><small class="glyphicon glyphicon-sort-by-attributes-alt"></small></span>

  val fields: List[((String, Elem), () => AnyVal)] = List(
    ("Number of fights", none("#")) -> fights,
    ("Wins", desc("W")) -> wins,
    ("Clean hits against", asc("CA")) -> cleanHitsReceived,
    ("Clean hits", desc("C")) -> cleanHitsDealt,
    ("Afterblows against", asc("AA")) -> afterblowsReceived,
    ("Double hits", asc("D")) -> doubleHits)
}

abstract class EmagRuleset extends Ruleset {
  type Scores = ParticipantScores

  val emptyScore = ParticipantScores(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)

  def compare(s1: ParticipantScores, s2: ParticipantScores)(implicit random: scala.util.Random) = {
    (s1, s2) match {
      case (ParticipantScores(i1, f1, w1, _, lbd1, cr1, cd1, ar1, _, d1, p1), ParticipantScores(i2, f2, w2, _, lbd2, cr2, cd2, ar2, _, d2, p2)) =>
        val effectivePoints1 = p1 - lbd1
        val effectivePoints2 = p2 - lbd2

        if (f1 == 0 && f2 == 0) {
          // if both haven't fought yet: order by initial ranking
          i1 > i2
        } else if (f1.min(f2) == 0) {
          // put fighters who have fought before those who haven't
          f2 == 0
        } else {
          // rank according to Mexico 2014 rules
          if (w1 != w2) {
            // a. most wins
            w1 > w2
          } else if (cr1 != cr2) {
            // b. fewest clean hits against
            cr1 < cr2
          } else if (cd1 != cd2) {
            // d. most clean hits dealt
            cd1 > cd2
          } else if (ar1 != ar2) {
            // f. fewest afterblows received
            ar1 < ar2
          } else if (d1 != d2) {
            // g. fewest doubles
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
    val result = pts.map(pt => (pt -> fs.foldLeft(ParticipantScores(pt.initialRanking(t), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)) {
      case (ps @ ParticipantScores(i, c, w, l, lbd, hR, hD, aR, aD, d, p), f) =>
        if (!f.cancelled.is && f.inFight_?(pt)) {
          f.currentScore match {
            case TotalScore(a, aafter, b, bafter, double, _) if a == b && f.fighterA.participant.get.id.is == pt.id.is =>
              ParticipantScores(i, c + 1, w, l, lbd + lossesByDoubles(double), hR + b, hD + a, aR + aafter, aD + bafter, d + double, a + p)
            case TotalScore(a, aafter, b, bafter, double, _) if a == b && f.fighterB.participant.get.id.is == pt.id.is =>
              ParticipantScores(i, c + 1, w, l, lbd + lossesByDoubles(double), hR + a, hD + b, aR + bafter, aD + aafter, d + double, b + p)
            case TotalScore(a, aafter, b, bafter, double, _) if a > b && f.fighterA.participant.get.id.is == pt.id.is =>
              ParticipantScores(i, c + 1, w + 1, l, lbd + lossesByDoubles(double), hR + b, hD + a, aR + aafter, aD + bafter, d + double, a + p)
            case TotalScore(a, aafter, b, bafter, double, _) if a > b && f.fighterB.participant.get.id.is == pt.id.is =>
              ParticipantScores(i, c + 1, w, l + 1, lbd + lossesByDoubles(double), hR + a, hD + b, aR + bafter, aD + aafter, d + double, b + p)
            case TotalScore(a, aafter, b, bafter, double, _) if a < b && f.fighterA.participant.get.id.is == pt.id.is =>
              ParticipantScores(i, c + 1, w, l + 1, lbd + lossesByDoubles(double), hR + b, hD + a, aR + aafter, aD + bafter, d + double, a + p)
            case TotalScore(a, aafter, b, bafter, double, _) if a < b && f.fighterB.participant.get.id.is == pt.id.is =>
              ParticipantScores(i, c + 1, w + 1, l, lbd + lossesByDoubles(double), hR + a, hD + b, aR + bafter, aD + aafter, d + double, b + p)
            case _ => ParticipantScores(i, c, w, l, lbd, hR, hD, aR, aD, d, p)
          }
        } else {
          ps
        }
    })).sortWith((pt1, pt2) => compare(pt1._2, pt2._2))
    def droppedOut(part: Participant) = part.subscription(p.tournament).map(_.droppedOut.is).getOrElse(true)
    // Always put dropped out fighters last
    result.filter(r => !droppedOut(r._1)) ++ result.filter(r => droppedOut(r._1))
  }

  def lossesByDoubles(doubles: Int): Int = if (fightProperties.doubleHitLimit > 0 && doubles >= fightProperties.doubleHitLimit) 1 else 0

  val possiblePoints = List(0, 1, 2)

  def fightProperties = FightProperties(
    timeLimit = 3 minutes,
    breakAt = 0,
    breakDuration = 0,
    timeBetweenFights = 2 minutes,
    exchangeLimit = 0,
    doubleHitLimit = 5)
}

trait PoolPhaseRuleset extends EmagRuleset

trait EliminationRuleset extends EmagRuleset {
  abstract override def fightProperties = super.fightProperties.copy(doubleHitLimit = 0)
}

trait FinalsRuleset extends EliminationRuleset {
  abstract override def fightProperties = super.fightProperties.copy(timeLimit = 6 minutes, breakAt = 3 minutes, breakDuration = 1 minute, exchangeLimit = 0)
}

trait DefaultRuleset extends EmagRuleset

object DefaultPoolPhaseRuleset extends DefaultRuleset with PoolPhaseRuleset {
  val id = "emag-2014-default-pools"
}

object DefaultEliminationRuleset extends DefaultRuleset with EliminationRuleset {
  val id = "emag-2014-default-elimination"
}

object DefaultFinalsRuleset extends DefaultRuleset with FinalsRuleset {
  val id = "emag-2014-default-finals"
}

trait AlbionRuleset extends DefaultRuleset {
  abstract override def fightProperties = super.fightProperties.copy(exchangeLimit = 10)
}

object AlbionPoolPhaseRuleset extends AlbionRuleset with PoolPhaseRuleset {
  val id = "emag-2014-albion-pools"
}

object AlbionEliminationRuleset extends AlbionRuleset with EliminationRuleset {
  val id = "emag-2014-albion-elimination"
}

object AlbionFinalsRuleset extends AlbionRuleset with FinalsRuleset {
  val id = "emag-2014-albion-finals"
}
