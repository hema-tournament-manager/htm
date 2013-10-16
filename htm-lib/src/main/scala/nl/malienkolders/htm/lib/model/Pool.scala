package nl.malienkolders.htm.lib
package model

import net.liftweb._
import mapper._

case class MarshalledPoolSummary(id: Long, order: Long, startTime: Long, round: MarshalledRoundSummary, fightCount: Long, participantsCount: Long)
case class MarshalledPool(id: Long, startTime: Long, order: Long, fights: List[Long], participants: List[MarshalledParticipant])
case class MarshalledViewerPool(summary: MarshalledPoolSummary, fights: List[MarshalledViewerFightSummary])
case class MarshalledPoolRanking(poolInfo: MarshalledPoolSummary, ranked: List[MarshalledParticipant], points: List[swiss.ParticipantScores])

class Pool extends LongKeyedMapper[Pool] with OneToMany[Long, Pool] with ManyToMany {
  def getSingleton = Pool

  def primaryKeyField = id
  object id extends MappedLongIndex(this)

  object startTime extends MappedLong(this)

  object order extends MappedLong(this)
  object round extends MappedLongForeignKey(this, Round)
  object fights extends MappedOneToMany(Fight, Fight.pool, OrderBy(Fight.order, Ascending)) with Owned[Fight] with Cascade[Fight]
  object participants extends MappedManyToMany(PoolParticipants, PoolParticipants.pool, PoolParticipants.participant, Participant)
  object arena extends MappedLongForeignKey(this, Arena)

  def nextFight = fights.filter(f => f.inProgress == false && f.finished_? == false).headOption

  def addFight(a: Participant, b: Participant) = fights += Fight.create.fighterA(a).fighterB(b).inProgress(false).order(fights.size + 1)

  def toMarshalled = MarshalledPool(id.is, startTime.is, order.is, fights.map(_.id.is).toList, participants.map(_.toMarshalled).toList)
  def toViewer = MarshalledViewerPool(toMarshalledSummary, fights.map(_.toViewerSummary).toList)
  def toMarshalledSummary = MarshalledPoolSummary(
    id.is,
    order.is,
    startTime.is,
    round.obj.get.toMarshalledSummary,
    fights.size,
    participants.size)
  def toMarshalledRanking = {
    val (pts, ss) = SwissTournament.ranking(this).unzip
    MarshalledPoolRanking(toMarshalledSummary, pts.map(_.toMarshalled), ss)
  }

  def poolName: String = {
    ('A'.toInt + (order.get - 1)).toChar.toString;
  }

}
object Pool extends Pool with LongKeyedMetaMapper[Pool]

class PoolParticipants extends LongKeyedMapper[PoolParticipants] with IdPK {
  def getSingleton = PoolParticipants
  object pool extends MappedLongForeignKey(this, Pool)
  object participant extends MappedLongForeignKey(this, Participant)
}
object PoolParticipants extends PoolParticipants with LongKeyedMetaMapper[PoolParticipants]