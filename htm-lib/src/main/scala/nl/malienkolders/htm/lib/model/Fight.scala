package nl.malienkolders.htm.lib
package model

import net.liftweb._
import mapper._
import common._
import util._
import Helpers._
import scala.xml._

case class MarshalledFight(id: Long, pool: Long, round: Long, order: Long, fighterA: MarshalledParticipant, fighterB: MarshalledParticipant, plannedTime: Long, timeStart: Long, timeStop: Long, netDuration: Long, scores: List[MarshalledScore])
case class MarshalledFightSummary(id: Long, poolId: Long, order: Long, fighterA: MarshalledParticipant, fighterB: MarshalledParticipant, plannedTime: Long, timeStart: Long, timeStop: Long, netDuration: Long, scores: List[MarshalledScore])
case class MarshalledViewerFight(tournament: MarshalledTournamentSummary, roundName: String, order: Long, exchangeLimit: Int, timeLimit: Long, fighterA: MarshalledParticipant, fighterB: MarshalledParticipant)
case class MarshalledViewerFightSummary(order: Long, fighterA: MarshalledParticipant, fighterB: MarshalledParticipant, started: Boolean, finished: Boolean, score: TotalScore)

class Fight extends LongKeyedMapper[Fight] with IdPK with CreatedUpdated with OneToMany[Long, Fight] {
  def getSingleton = Fight

  object pool extends MappedLongForeignKey(this, Pool)
  object order extends MappedLong(this)
  object inProgress extends MappedBoolean(this)
  object fighterA extends MappedLongForeignKey(this, Participant)
  object fighterB extends MappedLongForeignKey(this, Participant)
  object scores extends MappedOneToMany(Score, Score.fight, OrderBy(Score.timeInFight, Ascending)) with Owned[Score] with Cascade[Score]
  object timeStart extends MappedLong(this)
  object timeStop extends MappedLong(this)
  object netDuration extends MappedLong(this)

  def started_? = timeStart.is > 0 || inProgress.is
  def finished_? = timeStop.is > 0
  def grossDuration = timeStop.is - timeStart.is

  def currentScore = scores.foldLeft(TotalScore(0, 0, 0, 0, 0, 0, 0, 0)) { (sum, score) =>
    TotalScore(
      sum.a + score.diffA.get,
      sum.aAfter + score.diffAAfterblow.get,
      sum.b + score.diffB.get,
      sum.bAfter + score.diffBAfterblow.get,
      sum.double + score.diffDouble.get,
      sum.specialHitsA + (if (score.isSpecial.get && (score.diffA.get > 0 || score.diffDouble.get > 0)) 1 else 0),
      sum.specialHitsB + (if (score.isSpecial.get && (score.diffB.get > 0 || score.diffDouble.get > 0)) 1 else 0),
      sum.exchangeCount + (if (score.isExchange.get) 1 else 0))
  }

  def inFight_?(p: Participant) = fighterA.is == p.id.is || fighterB.is == p.id.is

  def winner = currentScore match {
    case TotalScore(a, _, b, _, _, _, _, _) if a > b => Full(fighterA.obj.get)
    case TotalScore(a, _, b, _, _, _, _, _) if a < b => Full(fighterB.obj.get)
    case _ => Empty
  }

  def shortLabel = fighterA.obj.get.name + " vs " + fighterB.obj.get.name

  def toMarshalled = MarshalledFight(id.is, pool.is, pool.obj.get.round.is, order.is, fighterA.obj.get.toMarshalled, fighterB.obj.get.toMarshalled, plannedStartTime, timeStart.is, timeStop.is, netDuration.is, scores.map(_.toMarshalled).toList)
  def toMarshalledSummary = MarshalledFightSummary(id.is, pool.is, order.is, fighterA.obj.get.toMarshalled, fighterB.obj.get.toMarshalled, plannedStartTime, timeStart.is, timeStop.is, netDuration.is, scores.map(_.toMarshalled).toList)
  def fromMarshalled(m: MarshalledFight) = {
    timeStart(m.timeStart)
    timeStop(m.timeStop)
    netDuration(m.netDuration)
    scores.clear
    m.scores.foreach(s => scores += Score.create.fromMarshalled(s))
    this
  }
  def fromMarshalledSummary(m: MarshalledFightSummary) = {
    if (timeStart.get == 0) {
      timeStart(m.timeStart)
    }
    timeStop(m.timeStop)
    netDuration(m.netDuration)
    m.scores.drop(scores.size).foreach(s => scores += Score.create.fromMarshalled(s))
    this
  }
  def toViewer = {
    val r = pool.obj.get.round.obj.get
    MarshalledViewerFight(r.tournament.obj.get.toMarshalledSummary, r.name.is, order.is, r.exchangeLimit.is, r.timeLimitOfFight.is, fighterA.obj.get.toMarshalled, fighterB.obj.get.toMarshalled)
  }
  def toViewerSummary = {
    MarshalledViewerFightSummary(
      order.is,
      fighterA.obj.get.toMarshalled,
      fighterB.obj.get.toMarshalled,
      started_?,
      finished_?,
      currentScore)
  }

  def plannedStartTime: Long = {

    val pool = this.pool.foreign.get;
    val round = pool.round.foreign.get;

    val result = pool.startTime.get + (round.timeLimitOfFight.get + round.timeBetweenFights.get + round.breakDuration.get) * (order.get - 1)

    return result;
  }

  def plannedEndTime: Long = {
    val pool = this.pool.foreign.get;
    val round = pool.round.foreign.get;

    return plannedStartTime + round.timeLimitOfFight.get + round.breakDuration.get;
  }
}

object Fight extends Fight with LongKeyedMetaMapper[Fight] with CRUDify[Long, Fight] {
  override def dbTableName = "fights"
}