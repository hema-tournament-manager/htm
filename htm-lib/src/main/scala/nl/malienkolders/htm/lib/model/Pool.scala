package nl.malienkolders.htm.lib
package model

import net.liftweb._
import mapper._
import nl.malienkolders.htm.lib.rulesets.Scores


class Pool extends LongKeyedMapper[Pool] with OneToMany[Long, Pool] with ManyToMany {
  def getSingleton = Pool

  def primaryKeyField = id
  object id extends MappedLongIndex(this)

  object startTime extends MappedLong(this)

  object order extends MappedLong(this)
  object phase extends MappedLongForeignKey(this, PoolPhase)
  object fights extends MappedOneToMany(PoolFight, PoolFight.pool, OrderBy(PoolFight.order, Ascending)) with Owned[PoolFight] with Cascade[PoolFight]
  object participants extends MappedManyToMany(PoolParticipants, PoolParticipants.pool, PoolParticipants.participant, Participant)
  object arena extends MappedLongForeignKey(this, Arena)

  def nextFight = fights.filter(f => f.inProgress == false && f.finished_? == false).headOption

  def finished_? = fights.map(_.finished_?).toList.forall(_ == true)

  def addFight(a: Participant, b: Participant) = fights += PoolFight.create.fighterAFuture(SpecificFighter(Some(a)).format).fighterBFuture(SpecificFighter(Some(b)).format).inProgress(false).order(fights.size + 1)

  
  def poolName: String = {
    ('A'.toInt + (order.get - 1)).toChar.toString;
  }

  def ranked: List[(Participant, Scores)] =
    phase.foreign.get.rulesetImpl.ranking(this)

  def tournament = phase.foreign.get.tournament.foreign.get
}
object Pool extends Pool with LongKeyedMetaMapper[Pool] {
  def defaultArena(tournament: Tournament): Arena = {
    tournament.defaultArena.foreign.getOrElse(Arena.default)
  }

  def create(t: Tournament) = super.create.arena(defaultArena(t)).startTime(System.currentTimeMillis())
}

class PoolParticipants extends LongKeyedMapper[PoolParticipants] with IdPK {
  def getSingleton = PoolParticipants
  object pool extends MappedLongForeignKey(this, Pool)
  object participant extends MappedLongForeignKey(this, Participant)
}
object PoolParticipants extends PoolParticipants with LongKeyedMetaMapper[PoolParticipants]