package nl.malienkolders.htm.lib
package model

import net.liftweb._
import mapper._
import common._
import util._
import Helpers._
import scala.xml._

case class FightId(phase: String, id: Long)

case class MarshalledFight(
  tournament: MarshalledTournamentSummary,
  phaseType: String,
  id: Long,
  name: String,
  fighterA: MarshalledFighter,
  fighterB: MarshalledFighter,
  timeStart: Long,
  timeStop: Long,
  netDuration: Long,
  scores: List[MarshalledScore],
  timeLimit: Long,
  exchangeLimit: Int,
  possiblePoints: List[Int],
  doubleHitLimit: Int)

trait Fight[F <: Fight[F, S], S <: Score[S, F]] extends LongKeyedMapper[F] with IdPK with FightToScore[F, S] {

  self: F =>

  object name extends MappedString(this, 128)
  object inProgress extends MappedBoolean(this)
  object fighterAFuture extends MappedString(this, 16)
  def fighterAFuture(f: Fighter): F = fighterAFuture(f.format)
  def fighterAFuture(p: Participant): F = fighterAFuture(SpecificFighter(Some(p)))
  object fighterBFuture extends MappedString(this, 16)
  def fighterBFuture(f: Fighter): F = fighterBFuture(f.format)
  def fighterBFuture(p: Participant): F = fighterBFuture(SpecificFighter(Some(p)))
  object timeStart extends MappedLong(this)
  object timeStop extends MappedLong(this)
  object netDuration extends MappedLong(this)
  object cancelled extends MappedBoolean(this) {
    override val defaultValue = false
  }

  def phaseType: PhaseType
  def phase: MappedLongForeignKey[_, _ <: Phase[_]]
  def tournament = phase.foreign.get.tournament.foreign.get
  def scheduled: MappedLongForeignKey[_, _ <: ScheduledFight[_]]

  def started_? = timeStart.is > 0 || inProgress.is
  def finished_? = cancelled.is || timeStop.is > 0
  def grossDuration = timeStop.is - timeStart.is

  def createScore: S = {
    scoreMeta.create
  }

  def addScore(score: Any) = {
    scores += score.asInstanceOf[S]
    score
  }

  def addScore = {
    val score = scoreMeta.create
    scores += score
    score
  }

  def mapScores[A](map: Score[_, _] => A): Seq[A] = scores.map(map)

  def currentScore = scores.foldLeft(TotalScore(0, 0, 0, 0, 0, 0)) { (sum, score) =>
    TotalScore(
      sum.red + score.pointsRed.get,
      sum.redAfter + score.afterblowsRed.get,
      sum.blue + score.pointsBlue.get,
      sum.blueAfter + score.afterblowsBlue.get,
      sum.double + score.doubles.get,
      sum.exchangeCount + score.exchanges.get)
  }

  def inFight_?(p: Participant) = (for {
    a <- fighterA.participant
    b <- fighterB.participant
  } yield a.id.is == p.id.is || b.id.is == p.id.is) getOrElse false

  def fighterA: Fighter = Fighter.parse(fighterAFuture.get)
  def fighterB: Fighter = Fighter.parse(fighterBFuture.get)

  def winner = cancelled.get match {
    case true =>
      // cancelled fights cannot be won
      Empty
    case false =>
      currentScore match {
        case TotalScore(a, _, b, _, _, _) if a > b => Full(fighterA.participant.get)
        case TotalScore(a, _, b, _, _, _) if a < b => Full(fighterB.participant.get)
        case _ => Empty
      }
  }

  def loser = cancelled.get match {
    case true =>
      // cancelled fights cannot be lost
      Empty
    case false =>
      currentScore match {
        case TotalScore(a, _, b, _, _, _) if a < b => Full(fighterA.participant.get)
        case TotalScore(a, _, b, _, _, _) if a > b => Full(fighterB.participant.get)
        case _ => Empty
      }
  }

  def opponent(fighter: Participant) = (for {
    a <- fighterA.participant
    b <- fighterB.participant
  } yield {
    if (a.id.get == fighter.id.get) {
      Some(b)
    } else if (b.id.get == fighter.id.get) {
      Some(a)
    } else {
      None
    }
  }).getOrElse(None)

  def shortLabel = fighterA.toString + " vs " + fighterB.toString

  def toMarshalled = MarshalledFight(
    phase.foreign.get.tournament.foreign.get.toMarshalledSummary,
    phaseType.code,
    id.is,
    name.is,
    fighterA.toMarshalled(phase.foreign.get.tournament.foreign.get),
    fighterB.toMarshalled(phase.foreign.get.tournament.foreign.get),
    timeStart.is,
    timeStop.is,
    netDuration.is,
    scores.map(_.toMarshalled).toList,
    phase.foreign.get.rulesetImpl.fightProperties.timeLimit,
    phase.foreign.get.rulesetImpl.fightProperties.exchangeLimit,
    phase.foreign.get.rulesetImpl.possiblePoints,
    phase.foreign.get.rulesetImpl.fightProperties.doubleHitLimit)
  def fromMarshalled(m: MarshalledFight) = {
    timeStart(m.timeStart)
    timeStop(m.timeStop)
    netDuration(m.netDuration)
    scores.clear
    m.scores.foreach(s => scores += scoreMeta.create.fromMarshalled(s))
    this
  }
  def fromMarshalledSummary(m: MarshalledFight) = {
    if (timeStart.get == 0) {
      timeStart(m.timeStart)
    }
    timeStop(m.timeStop)
    netDuration(m.netDuration)
    m.scores.drop(scores.size).foreach(s => scores += scoreMeta.create.fromMarshalled(s))
    this
  }

  def schedule(time: Long, duration: Long): ScheduledFight[_]

  def sameFighters(other: Fight[_, _]) = {
    val myFighters = fighterA :: fighterB :: Nil
    val theirFighters = other.fighterA :: other.fighterB :: Nil
    myFighters.filterNot(my => theirFighters.exists(_.sameAs(my))).isEmpty
  }
}

object FightHelper {
  def dao(phaseType: PhaseType): LongKeyedMetaMapper[_ <: Fight[_, _]] = phaseType match {
    case PoolType => PoolFight
    case EliminationType => EliminationFight
    case _ => PoolFight
  }

  def dao(phaseType: String): LongKeyedMetaMapper[_ <: Fight[_, _]] = phaseType match {
    case PoolType.code => PoolFight
    case EliminationType.code => EliminationFight
    case _ => PoolFight
  }
}

case class MarshalledViewerPoolFightSummary(order: Long, fighterA: MarshalledParticipant, fighterB: MarshalledParticipant, started: Boolean, finished: Boolean, score: TotalScore)

class PoolFight extends Fight[PoolFight, PoolFightScore] {
  def getSingleton = PoolFight

  def scoreMeta = PoolFightScore

  object pool extends MappedLongForeignKey(this, Pool)
  object order extends MappedLong(this)
  object scheduled extends MappedLongForeignKey(this, ScheduledPoolFight)

  def phase = pool.foreign.get.phase
  val phaseType = PoolType

  def toViewerSummary = MarshalledViewerPoolFightSummary(
    order.is,
    fighterA.participant.get.toMarshalled,
    fighterB.participant.get.toMarshalled,
    started_?,
    finished_?,
    currentScore)

  def schedule(time: Long, duration: Long) = {
    val sf = ScheduledPoolFight.create.fight(this).time(time).duration(duration)
    scheduled(sf)
    sf
  }
}

object PoolFight extends PoolFight with LongKeyedMetaMapper[PoolFight] {
  override def delete_! = {
    scheduled.foreign.foreach(_.delete_!)
    super.delete_!
  }
}

class EliminationFight extends Fight[EliminationFight, EliminationFightScore] {
  def getSingleton = EliminationFight

  def scoreMeta = EliminationFightScore

  object phase extends MappedLongForeignKey(this, EliminationPhase)
  val phaseType = EliminationType

  object round extends MappedLong(this)
  object scheduled extends MappedLongForeignKey(this, ScheduledEliminationFight)

  def schedule(time: Long, duration: Long) = {
    val sf = ScheduledEliminationFight.create.fight(this).time(time).duration(duration)
    scheduled(sf)
    sf
  }
}
object EliminationFight extends EliminationFight with LongKeyedMetaMapper[EliminationFight] {
  override def delete_! = {
    scheduled.foreign.foreach(_.delete_!)
    super.delete_!
  }
}