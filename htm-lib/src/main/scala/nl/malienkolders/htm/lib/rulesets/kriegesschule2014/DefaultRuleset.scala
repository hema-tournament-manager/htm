package nl.malienkolders.htm.lib.rulesets
package kriegesschule2014

import nl.malienkolders.htm.lib.model._
import nl.malienkolders.htm.lib.util.Helpers._
import net.liftweb.mapper._
import net.liftweb.util.TimeHelpers._
import scala.xml.Elem

case class ParticipantScores(
    fights: Int,
    wins: Int,
    lossesByDoubles: Int,
    cleanHitsDealt: Int,
    cleanHitsReceived: Int,
    doubleHits: Int,
    exchangePoints: Int,
    hitsReceived: Int) extends Scores {

  def none(caption: String) = <span>{ caption }</span>
  def asc(caption: String) = <span><span>{ caption }</span><small class="glyphicon glyphicon-sort-by-attributes"></small></span>
  def desc(caption: String) = <span><span>{ caption }</span><small class="glyphicon glyphicon-sort-by-attributes-alt"></small></span>

  val numberOfFights = fights

  val fields: List[ScoreField] = List(
    ScoreField("Wins", desc("W"), HighestFirst, wins),
    ScoreField("Points", desc("P"), HighestFirst, wins),
    ScoreField("hits against", asc("HA"), LowestFirst, hitsReceived),
    ScoreField("Double hits", asc("D"), LowestFirst, doubleHits))
}

abstract class KriegesSchuleRuleset extends Ruleset {
  type Scores = ParticipantScores

  val emptyScore = ParticipantScores(0, 0, 0, 0, 0, 0, 0, 0)

  def compare(s1: ParticipantScores, s2: ParticipantScores)(implicit random: scala.util.Random) = {
    (s1, s2) match {
      case (ParticipantScores(f1, w1, lbd1, cd1, cr1, d1, p1, hr1), ParticipantScores(f2, w2, lbd2, cd2, cr2, d2, p2, hr2)) =>
                
      	// Ranking of people overall should be done by
        // - number of wins
        // - overall points
        // - least number of hits against (how many times they were hit by an opponent, including clean hits and afterblows)
        // - least number of doubles 
        if (w1 != w2) {
          w1 > w2
        } else if (p1 != p2) {
          p1 > p2
        } else if (hr1 != hr2) {
          hr1 < hr2
        } else if (d1 != d2) {
          d1 < d2
        } else {
          random.nextBoolean
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
    val result = pts.map(pt => (pt -> fs.foldLeft(ParticipantScores(0, 0, 0, 0, 0, 0, 0, 0)) {
      case (ps @ ParticipantScores(
          c /*fight count*/,
          w /*wins*/,
          lbd /*losses by doubles*/,
          chD /*clean hits dealt*/,
          chR /*clean hits received*/,
          d /*double hits*/,
          p /*points*/,
          hR /*hits received*/), f) =>
        if (!f.cancelled.is && f.inFight_?(pt)) {
          f.currentScore match {
            case TotalScore(a, aafter, b, bafter, double, _, aclean, bclean) if a == b && f.fighterA.participant.get.id.is == pt.id.is =>
              ParticipantScores(c + 1, w, lbd + lossesByDoubles(double), chD + a, chR + b, d + double, p + a, hR + bclean + bafter)
            case TotalScore(a, aafter, b, bafter, double, _, aclean, bclean) if a == b && f.fighterB.participant.get.id.is == pt.id.is =>
              ParticipantScores(c + 1, w, lbd + lossesByDoubles(double), chD + b, chR + a, d + double, b + p, hR + aclean + aafter)
            case TotalScore(a, aafter, b, bafter, double, _, aclean, bclean) if a > b && f.fighterA.participant.get.id.is == pt.id.is =>
              ParticipantScores(c + 1, w + 1, lbd + lossesByDoubles(double), chD + a, chR + b, d + double, a + p, hR + bclean + bafter)
            case TotalScore(a, aafter, b, bafter, double, _, aclean, bclean) if a > b && f.fighterB.participant.get.id.is == pt.id.is =>
              ParticipantScores(c + 1, w, lbd + lossesByDoubles(double), chD + b, chR + a, d + double, b + p, hR + aclean + aafter)
            case TotalScore(a, aafter, b, bafter, double, _, aclean, bclean) if a < b && f.fighterA.participant.get.id.is == pt.id.is =>
              ParticipantScores(c + 1, w, lbd + lossesByDoubles(double), chD + a, chR + b, d + double, a + p, hR + bclean + bafter)
            case TotalScore(a, aafter, b, bafter, double, _, aclean, bclean) if a < b && f.fighterB.participant.get.id.is == pt.id.is =>
              ParticipantScores(c + 1, w + 1, lbd + lossesByDoubles(double), chD + b, chR + a, d + double, b + p, hR + aclean + aafter)
            case _ => ParticipantScores(c, w, lbd, chD, chR, d, p, hR)
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

  val possiblePoints = (0 to 12).toList

  def fightProperties = FightProperties(
    timeLimit = 3 minutes,
    breakAt = 0,
    breakDuration = 0,
    timeBetweenFights = 0,
    exchangeLimit = 0,
    doubleHitLimit = 3,
    pointLimit = 7)
}

object KriegesSchuleRuleset {
  def registerAll(): Unit = {
    DefaultPoolPhaseRuleset.register()
    DefaultEliminationRuleset.register()
    DefaultFinalsRuleset.register()
  }
}

trait PoolPhaseRuleset extends KriegesSchuleRuleset

trait EliminationRuleset extends KriegesSchuleRuleset {
  abstract override def fightProperties = super.fightProperties.copy(doubleHitLimit = 0)
}

trait FinalsRuleset extends EliminationRuleset {
  abstract override def fightProperties = super.fightProperties.copy(timeLimit = 6 minutes, breakAt = 3 minutes, breakDuration = 1 minute, exchangeLimit = 0)
}

trait DefaultRuleset extends KriegesSchuleRuleset

object DefaultPoolPhaseRuleset extends DefaultRuleset with PoolPhaseRuleset {
  val id = "kriegesschule-2014-default-pools"
}

object DefaultEliminationRuleset extends DefaultRuleset with EliminationRuleset {
  val id = "kriegesschule-2014-default-elimination"
}

object DefaultFinalsRuleset extends DefaultRuleset with FinalsRuleset {
  val id = "kriegesschule-2014-default-finals"
}