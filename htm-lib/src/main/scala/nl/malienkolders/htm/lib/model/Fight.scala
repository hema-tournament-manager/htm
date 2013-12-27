package nl.malienkolders.htm.lib
package model

import net.liftweb._
import mapper._
import common._
import util._
import Helpers._
import scala.xml._

case class MarshalledFight(fighterA: MarshalledParticipant, fighterB: MarshalledParticipant, timeStart: Long, timeStop: Long, netDuration: Long, scores: List[MarshalledScore])
case class MarshalledFightSummary(fighterA: MarshalledParticipant, fighterB: MarshalledParticipant, timeStart: Long, timeStop: Long, netDuration: Long, scores: List[MarshalledScore])

trait Fight[F <: Fight[F, S], S <: Score[S, F]] extends LongKeyedMapper[F] with FightToScore[F, S] {
  
  self: F =>
    
  object tournament extends MappedLongForeignKey(this, Tournament)
  object inProgress extends MappedBoolean(this)
  object fighterA extends MappedLongForeignKey(this, Participant)
  object fighterB extends MappedLongForeignKey(this, Participant)
  object timeStart extends MappedLong(this)
  object timeStop extends MappedLong(this)
  object netDuration extends MappedLong(this)

  def started_? = timeStart.is > 0 || inProgress.is
  def finished_? = timeStop.is > 0
  def grossDuration = timeStop.is - timeStart.is

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

  def toMarshalled = MarshalledFight(fighterA.obj.get.toMarshalled, fighterB.obj.get.toMarshalled, timeStart.is, timeStop.is, netDuration.is, scores.map(_.toMarshalled).toList)
  def toMarshalledSummary = MarshalledFightSummary(fighterA.obj.get.toMarshalled, fighterB.obj.get.toMarshalled, timeStart.is, timeStop.is, netDuration.is, scores.map(_.toMarshalled).toList)
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

class PoolFight extends Fight[PoolFight, PoolFightScore] with IdPK {
  def getSingleton = PoolFight
  
  def scoreMeta = PoolFightScore
  
  object pool extends MappedLongForeignKey(this, Pool)
  object order extends MappedLong(this)
  
}
object PoolFight extends PoolFight with LongKeyedMetaMapper[PoolFight]

class EliminationFight extends Fight[EliminationFight, EliminationFightScore] with IdPK {
  def getSingleton = EliminationFight
  
  def scoreMeta = EliminationFightScore
  
  object phase extends MappedLongForeignKey(this, EliminationPhase)
  object round extends MappedLong(this)
}
object EliminationFight extends EliminationFight with LongKeyedMetaMapper[EliminationFight]