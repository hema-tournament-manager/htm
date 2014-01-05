package nl.malienkolders.htm.lib
package model

import net.liftweb._
import mapper._
import common._
import util._
import Helpers._
import scala.xml._

case class FightId(phase: String, id: Long)

case class MarshalledFight(phaseType: PhaseType, id: Long, fighterA: MarshalledParticipant, fighterB: MarshalledParticipant, timeStart: Long, timeStop: Long, netDuration: Long, scores: List[MarshalledScore])
case class MarshalledFightSummary(phaseType: PhaseType, id: Long, fighterA: MarshalledParticipant, fighterB: MarshalledParticipant, timeStart: Long, timeStop: Long, netDuration: Long, scores: List[MarshalledScore])

trait Fight[F <: Fight[F, S], S <: Score[S, F]] extends LongKeyedMapper[F] with IdPK with FightToScore[F, S] {

  self: F =>

  private val fightToPhaseType: Map[LongKeyedMapper[_], PhaseType] = Map(
    PoolFight -> PoolType,
    EliminationFight -> EliminationType)

  object tournament extends MappedLongForeignKey(this, Tournament)
  object inProgress extends MappedBoolean(this)
  object fighterA extends MappedLongForeignKey(this, Participant)
  object fighterB extends MappedLongForeignKey(this, Participant)
  object timeStart extends MappedLong(this)
  object timeStop extends MappedLong(this)
  object netDuration extends MappedLong(this)

  def phaseType = fightToPhaseType(getSingleton)
  def phase: MappedLongForeignKey[_, _ <: Phase[_]]
  def scheduled: MappedLongForeignKey[_, _ <: ScheduledFight[_]]

  def started_? = timeStart.is > 0 || inProgress.is
  def finished_? = timeStop.is > 0
  def grossDuration = timeStop.is - timeStart.is

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

  def inFight_?(p: Participant) = fighterA.is == p.id.is || fighterB.is == p.id.is

  def winner = currentScore match {
    case TotalScore(a, _, b, _, _, _) if a > b => Full(fighterA.obj.get)
    case TotalScore(a, _, b, _, _, _) if a < b => Full(fighterB.obj.get)
    case _ => Empty
  }

  def shortLabel = fighterA.obj.get.name + " vs " + fighterB.obj.get.name

  def toMarshalled = MarshalledFight(phaseType, id.is, fighterA.obj.get.toMarshalled, fighterB.obj.get.toMarshalled, timeStart.is, timeStop.is, netDuration.is, scores.map(_.toMarshalled).toList)
  def toMarshalledSummary = MarshalledFightSummary(phaseType, id.is, fighterA.obj.get.toMarshalled, fighterB.obj.get.toMarshalled, timeStart.is, timeStop.is, netDuration.is, scores.map(_.toMarshalled).toList)
  def fromMarshalled(m: MarshalledFight) = {
    timeStart(m.timeStart)
    timeStop(m.timeStop)
    netDuration(m.netDuration)
    scores.clear
    m.scores.foreach(s => scores += scoreMeta.create.fromMarshalled(s))
    this
  }
  def fromMarshalledSummary(m: MarshalledFightSummary) = {
    if (timeStart.get == 0) {
      timeStart(m.timeStart)
    }
    timeStop(m.timeStop)
    netDuration(m.netDuration)
    m.scores.drop(scores.size).foreach(s => scores += scoreMeta.create.fromMarshalled(s))
    this
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

  def toViewerSummary = MarshalledViewerPoolFightSummary(
    order.is,
    fighterA.foreign.get.toMarshalled,
    fighterB.foreign.get.toMarshalled,
    started_?,
    finished_?,
    currentScore)
}
object PoolFight extends PoolFight with LongKeyedMetaMapper[PoolFight]

class EliminationFight extends Fight[EliminationFight, EliminationFightScore] {
  def getSingleton = EliminationFight

  def scoreMeta = EliminationFightScore

  object phase extends MappedLongForeignKey(this, EliminationPhase)
  object round extends MappedLong(this)
  object scheduled extends MappedLongForeignKey(this, ScheduledEliminationFight)
}
object EliminationFight extends EliminationFight with LongKeyedMetaMapper[EliminationFight]