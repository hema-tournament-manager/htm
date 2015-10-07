package nl.malienkolders.htm.lib.rulesets
package heffac2015

import nl.malienkolders.htm.lib.model._
import nl.malienkolders.htm.lib.util.Helpers._
import net.liftweb.mapper._
import net.liftweb.util.TimeHelpers._
import scala.xml.Elem

case class ParticipantScores(
    fights: Int,
    wins: Int,
    ties: Int,
    cleanHitsDealt: Int,
    cleanHitsReceived: Int,
    doubleHits: Int) extends Scores {

  def none(caption: String) = <span>{ caption }</span>
  def asc(caption: String) = <span><span>{ caption }</span><small class="glyphicon glyphicon-sort-by-attributes"></small></span>
  def desc(caption: String) = <span><span>{ caption }</span><small class="glyphicon glyphicon-sort-by-attributes-alt"></small></span>

  def points = wins * 3 + ties

  val numberOfFights = fights

  val fields: List[ScoreField] = List(
    ScoreField("Punten", desc("P"), HighestFirst, points),
    ScoreField("Clean hits tegen", asc("CHT"), LowestFirst, cleanHitsReceived),
    ScoreField("Doubles", asc("D"), LowestFirst, doubleHits),
    ScoreField("Clean hits uitgedeeld", desc("CHU"), HighestFirst, cleanHitsDealt))
}

abstract class HeffacRuleset extends Ruleset {
  type Scores = ParticipantScores

  val emptyScore = ParticipantScores(0, 0, 0, 0, 0, 0)

  def compare(s1: ParticipantScores, s2: ParticipantScores)(implicit random: scala.util.Random) = {
    (s1, s2) match {
      case (ParticipantScores(f1, w1, t1, cd1, cr1, d1), ParticipantScores(f2, w2, t2, cd2, cr2, d2)) =>

        if (f1.min(f2) == 0) {
          // put fighters who have fought before those who haven't
          f2 == 0
        } else if (s1.points != s2.points) {
          s1.points > s2.points
        } else if (cr1 != cr2) {
          // Bij gelijk aantal punten aan het eind van de poule-fase gaat degene door die het minste aantal (clean) hits tegen heeft gekregen
          cr1 < cr2
        } else if (d1 != d2) {
          // Vervolgens wordt gekeken naar het minst aantal double hits
          d1 < d2
        } else if (cd1 != cd2) {
          // daarna het meest uitgedeelde [clean]hits
          cd1 > cd2
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
    val result = pts.map(pt => (pt -> fs.foldLeft(ParticipantScores(0, 0, 0, 0, 0, 0)) {
      case (ps @ ParticipantScores(c, w, t, cd, cr, d), f) =>
        if (!f.cancelled.is && f.inFight_?(pt)) {
          f.currentScore match {
            case TotalScore(a, _, b, _, dbl, _, ca, cb) if a > b && f.fighterA.participant.get.id.is == pt.id.is =>
              // win for A
              ParticipantScores(c + 1, w + 1, t, cd + ca, cr + cb, d + dbl)
            case TotalScore(a, _, b, _, dbl, _, ca, cb) if a == b && f.fighterA.participant.get.id.is == pt.id.is =>
              // tie for A
              ParticipantScores(c + 1, w, t + 1, cd + ca, cr + cb, d + dbl)
            case TotalScore(a, _, b, _, dbl, _, ca, cb) if a < b && f.fighterA.participant.get.id.is == pt.id.is =>
              // loss for A
              ParticipantScores(c + 1, w, t, cd + ca, cr + cb, d + dbl)
            case TotalScore(a, _, b, _, dbl, _, ca, cb) if a < b && f.fighterB.participant.get.id.is == pt.id.is =>
              // win for B
              ParticipantScores(c + 1, w + 1, t, cd + cb, cr + ca, d + dbl)
            case TotalScore(a, _, b, _, dbl, _, ca, cb) if a == b && f.fighterB.participant.get.id.is == pt.id.is =>
              // tie for B
              ParticipantScores(c + 1, w, t + 1, cd + cb, cr + ca, d + dbl)
            case TotalScore(a, _, b, _, dbl, _, ca, cb) if a > b && f.fighterB.participant.get.id.is == pt.id.is =>
              // loss for B
              ParticipantScores(c + 1, w, t, cd + cb, cr + ca, d + dbl)
            case _ => ParticipantScores(c, w, t, cr, cd, d)
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

  val possiblePoints = List(1, 2, 3)

  // Afterblow: 1 punt aftrek van de score behaald door de deelnemer die als eerste raakte.
  val possiblePointsAfterblow = possiblePoints.map(_ - 1)

  def fightProperties = FightProperties(
    timeLimit = 3 minutes,
    breakAt = 0,
    breakDuration = 0,
    timeBetweenFights = 2 minutes,
    exchangeLimit = 10,
    doubleHitLimit = 0,
    pointLimit = 0,
    possibleHits = List(
      Hit("Clean hit...", "clean", LeftSide, List(Scoring(ExchangePoints, PlusOne), Scoring(CleanHitsLeft, PlusOne), Scoring(PointsLeft, Pick(possiblePoints)))),
      Hit("Double", "double", CenterSide, List(Scoring(ExchangePoints, PlusOne), Scoring(DoubleHits, PlusOne))),
      Hit("Clean hit...", "clean", RightSide, List(Scoring(ExchangePoints, PlusOne), Scoring(CleanHitsRight, PlusOne), Scoring(PointsRight, Pick(possiblePoints)))),
      Hit("Afterblow...", "afterblow", LeftSide, List(Scoring(ExchangePoints, PlusOne), Scoring(AfterblowsLeft, PlusOne), Scoring(PointsLeft, Pick(possiblePointsAfterblow)))),
      Hit("Unclear hit", "none", CenterSide, List(Scoring(ExchangePoints, PlusOne))),
      Hit("Afterblow...", "afterblow", RightSide, List(Scoring(ExchangePoints, PlusOne), Scoring(AfterblowsRight, PlusOne), Scoring(PointsRight, Pick(possiblePointsAfterblow))))))
}

object HeffacRuleset {
  def registerAll(): Unit = {
    DefaultRuleset.register()
    FinalsRuleset.register()
  }
}

trait FinalsRuleset extends HeffacRuleset {
  abstract override def fightProperties = super.fightProperties.copy(
    timeLimit = 6 minutes,
    breakAt = 3 minutes,
    breakDuration = 2 minutes,
    exchangeLimit = 0)
}

object DefaultRuleset extends HeffacRuleset {
  val id = "heffac-2015-default"
}

object FinalsRuleset extends FinalsRuleset {
  val id = "heffac-2015-finals"

}