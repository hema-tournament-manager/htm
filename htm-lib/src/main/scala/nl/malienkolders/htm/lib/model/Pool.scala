package nl.malienkolders.htm.lib
package model

import net.liftweb._
import mapper._

case class MarshalledPoolSummary(id: Long, name: String, order: Long, startTime: Long, finished: Boolean, fightCount: Long, participantsCount: Long)
case class MarshalledPool(id: Long, name: String, startTime: Long, order: Long, fights: List[Long], participants: List[MarshalledParticipant])
case class MarshalledViewerPool(summary: MarshalledPoolSummary, fights: List[MarshalledViewerPoolFightSummary])

class Pool extends LongKeyedMapper[Pool] with OneToMany[Long, Pool] with ManyToMany {
  def getSingleton = Pool

  def primaryKeyField = id
  object id extends MappedLongIndex(this)

  object startTime extends MappedLong(this)

  object order extends MappedLong(this)
  object phase extends MappedLongForeignKey(this, PoolPhase)
  object fights extends MappedOneToMany(PoolFight, PoolFight.pool, OrderBy(PoolFight.id, Ascending)) with Owned[PoolFight] with Cascade[PoolFight]
  object participants extends MappedManyToMany(PoolParticipants, PoolParticipants.pool, PoolParticipants.participant, Participant)
  object arena extends MappedLongForeignKey(this, Arena)

  def nextFight = fights.filter(f => f.inProgress == false && f.finished_? == false).headOption

  def finished_? = fights.map(_.finished_?).toList.forall(_ == true)

  def addFight(a: Participant, b: Participant) = fights += PoolFight.create.fighterAFuture(SpecificFighter(Some(a)).format).fighterBFuture(SpecificFighter(Some(b)).format).inProgress(false).order(fights.size + 1)

  def toMarshalled = MarshalledPool(id.is, poolName, startTime.is, order.is, fights.map(_.id.is).toList, participants.map(_.toMarshalled).toList)
  def toMarshalledSummary = MarshalledPoolSummary(
    id.is,
    poolName,
    order.is,
    startTime.is,
    finished_?,
    fights.size,
    participants.size)
  def toViewer = MarshalledViewerPool(toMarshalledSummary, fights.map(_.toViewerSummary).toList)

  def poolName: String = {
    ('A'.toInt + (order.get - 1)).toChar.toString;
  }

  def ranked: List[Participant] =
    // TODO: ranking of participants
    participants.toList

}
object Pool extends Pool with LongKeyedMetaMapper[Pool] {
  def defaultArena(tournament: Tournament): Arena = {
    tournament.defaultArena.foreign.getOrElse(Arena.findAll.head)
  }

  def create(t: Tournament) = super.create.arena(defaultArena(t)).startTime(System.currentTimeMillis())
}

class PoolParticipants extends LongKeyedMapper[PoolParticipants] with IdPK {
  def getSingleton = PoolParticipants
  object pool extends MappedLongForeignKey(this, Pool)
  object participant extends MappedLongForeignKey(this, Participant)
}
object PoolParticipants extends PoolParticipants with LongKeyedMetaMapper[PoolParticipants]