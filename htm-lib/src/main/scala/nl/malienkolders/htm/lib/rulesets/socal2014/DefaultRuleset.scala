package nl.malienkolders.htm.lib.rulesets
package socal2014

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

  val numberOfFights = fights

  val fields: List[ScoreField] = List(
    ScoreField("Wins", desc("W"), HighestFirst, wins),
    ScoreField("Clean hits against", asc("CA"), LowestFirst, cleanHitsReceived),
    ScoreField("Clean hits", desc("C"), HighestFirst, cleanHitsDealt),
    ScoreField("Afterblows against", asc("AA"), LowestFirst, afterblowsReceived),
    ScoreField("Double hits", asc("D"), LowestFirst, doubleHits))
}

abstract class SocalRuleset extends Ruleset {
  type Scores = ParticipantScores

  val emptyScore = ParticipantScores(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)

  def compare(s1: ParticipantScores, s2: ParticipantScores)(implicit random: scala.util.Random) = {
    s1.wins.compareTo(s2.wins) > 0
  }

  def planning(pool: Pool): List[PoolFight] = Nil

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
            case TotalScore(a, aafter, b, bafter, double, _, _, _) if a == b && f.fighterA.participant.get.id.is == pt.id.is =>
              ParticipantScores(i, c + 1, w, l, lbd + lossesByDoubles(double), hR + b, hD + a, aR + aafter, aD + bafter, d + double, a + p)
            case TotalScore(a, aafter, b, bafter, double, _, _, _) if a == b && f.fighterB.participant.get.id.is == pt.id.is =>
              ParticipantScores(i, c + 1, w, l, lbd + lossesByDoubles(double), hR + a, hD + b, aR + bafter, aD + aafter, d + double, b + p)
            case TotalScore(a, aafter, b, bafter, double, _, _, _) if a > b && f.fighterA.participant.get.id.is == pt.id.is =>
              ParticipantScores(i, c + 1, w + 1, l, lbd + lossesByDoubles(double), hR + b, hD + a, aR + aafter, aD + bafter, d + double, a + p)
            case TotalScore(a, aafter, b, bafter, double, _, _, _) if a > b && f.fighterB.participant.get.id.is == pt.id.is =>
              ParticipantScores(i, c + 1, w, l + 1, lbd + lossesByDoubles(double), hR + a, hD + b, aR + bafter, aD + aafter, d + double, b + p)
            case TotalScore(a, aafter, b, bafter, double, _, _, _) if a < b && f.fighterA.participant.get.id.is == pt.id.is =>
              ParticipantScores(i, c + 1, w, l + 1, lbd + lossesByDoubles(double), hR + b, hD + a, aR + aafter, aD + bafter, d + double, a + p)
            case TotalScore(a, aafter, b, bafter, double, _, _, _) if a < b && f.fighterB.participant.get.id.is == pt.id.is =>
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

  val possiblePoints = (0 to 6).toList

  def fightProperties = FightProperties(
    timeLimit = (2 minutes) + (30 seconds),
    breakAt = 0,
    breakDuration = 0,
    timeBetweenFights = (2 minutes) + (30 seconds),
    exchangeLimit = 0,
    doubleHitLimit = 5,
    pointLimit = 12,
    possibleHits = List(
      Hit("Clean hit...", "clean", LeftSide, List(Scoring(ExchangePoints, PlusOne), Scoring(CleanHitsLeft, PlusOne), Scoring(PointsLeft, Pick(possiblePoints)))),
      Hit("Double", "double", CenterSide, List(Scoring(ExchangePoints, PlusOne), Scoring(DoubleHits, PlusOne))),
      Hit("Clean hit...", "clean", RightSide, List(Scoring(ExchangePoints, PlusOne), Scoring(CleanHitsRight, PlusOne), Scoring(PointsRight, Pick(possiblePoints)))),
      Hit("Afterblow...", "afterblow", LeftSide, List(Scoring(ExchangePoints, PlusOne), Scoring(AfterblowsLeft, PlusOne), Scoring(PointsLeft, Pick(possiblePoints)), Scoring(PointsRight, Pick(possiblePoints)))),
      Hit("No hit", "none", CenterSide, List(Scoring(ExchangePoints, PlusOne))),
      Hit("Afterblow...", "afterblow", RightSide, List(Scoring(ExchangePoints, PlusOne), Scoring(AfterblowsRight, PlusOne), Scoring(PointsRight, Pick(possiblePoints)), Scoring(PointsLeft, Pick(possiblePoints))))))
}

object LongswordRuleset extends SocalRuleset {
  val id = "socal-2014-longsword"
}

object SwordBucklerRuleset extends SocalRuleset {
  val id = "socal-2014-sword-buckler"
  override def fightProperties = super.fightProperties.copy(pointLimit = 7)
  override val possiblePoints = (0 to 3).toList
}