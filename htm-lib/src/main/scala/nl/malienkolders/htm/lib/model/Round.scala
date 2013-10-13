package nl.malienkolders.htm.lib
package model

import net.liftweb._
import mapper._

case class MarshalledRoundSummary(id: Long, order: Long, name: String, tournament: MarshalledTournamentSummary)
case class MarshalledRound(id: Long, order: Long, name: String, timeLimitOfFight: Long, breakInFightAt: Long, exchangeLimit: Int, breakDuration: Long, timeBetweenFights: Long, pools: List[MarshalledPoolSummary])

class Round extends LongKeyedMapper[Round] with OneToMany[Long, Round] {

  def getSingleton = Round

  def primaryKeyField = id
  object id extends MappedLongIndex(this)
  object order extends MappedLong(this)
  object tournament extends MappedLongForeignKey(this, Tournament)
  object name extends MappedString(this, 32)
  object timeLimitOfFight extends MappedLong(this)
  object breakInFightAt extends MappedLong(this)
  object exchangeLimit extends MappedInt(this)

  object breakDuration extends MappedLong(this)
  object timeBetweenFights extends MappedLong(this)

  object ruleset extends MappedString(this, 32)

  object pools extends MappedOneToMany(Pool, Pool.round, OrderBy(Pool.order, Ascending)) with Owned[Pool] with Cascade[Pool]

  def previousRound = {
    Round.find(By(Round.order, order.is - 1), By(Round.tournament, tournament.is))
  }

  def previousRounds: List[Round] = previousRound.map(prev => prev :: prev.previousRounds).getOrElse(List())

  def toMarshalled = MarshalledRound(id.is, order.is, name.is, timeLimitOfFight.is, breakInFightAt.is, exchangeLimit.is, breakDuration.is, timeBetweenFights.is, pools.map(_.toMarshalledSummary).toList)
  def toMarshalledSummary = MarshalledRoundSummary(id.is,
    order.is,
    name.is,
    tournament.obj.get.toMarshalledSummary)
}
object Round extends Round with LongKeyedMetaMapper[Round]
