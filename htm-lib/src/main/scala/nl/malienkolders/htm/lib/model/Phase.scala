package nl.malienkolders.htm.lib.model

import net.liftweb.mapper._
import nl.malienkolders.htm.lib.rulesets.Ruleset

trait Phase[P <: Phase[P]] extends LongKeyedMapper[P] with OneToMany[Long, P] {

  self: P =>

  object order extends MappedLong(this)

  object name extends MappedString(this, 128)

  object tournament extends MappedLongForeignKey(this, Tournament)

  object timeLimitOfFight extends MappedLong(this)
  object breakInFightAt extends MappedLong(this)
  object exchangeLimit extends MappedInt(this)

  object breakDuration extends MappedLong(this)
  object timeBetweenFights extends MappedLong(this)

  object ruleset extends MappedString(this, 32)

  def previousPhase = {
    tournament.foreign.get.phases.find(_.order.is == order.is - 1)
  }

  def previousPhases: List[Phase[_]] = tournament.foreign.get.phases.filter(_.order.is < order.is).toList

  def fights: Seq[Fight[_, _]]

  def rulesetImpl = Ruleset(ruleset.get)

}

class PoolPhase extends Phase[PoolPhase] with IdPK {

  def getSingleton = PoolPhase

  object pools extends MappedOneToMany(Pool, Pool.phase, OrderBy(Pool.order, Ascending)) with Owned[Pool] with Cascade[Pool]

  // the pool phase is finished when all pools are finished
  def finished_? = pools.map(_.finished_?).forall(x => x)

  def addPool: Pool = {
    val newPool = Pool.create(tournament.obj.get).order(pools.size + 1)
    pools += newPool
    newPool
  }

  def fights = pools.flatMap(_.fights)

}
object PoolPhase extends PoolPhase with LongKeyedMetaMapper[PoolPhase]

class EliminationPhase extends Phase[EliminationPhase] with IdPK {

  def getSingleton = EliminationPhase

  object eliminationFights extends MappedOneToMany(EliminationFight, EliminationFight.phase, OrderBy(EliminationFight.id, Ascending)) with Owned[EliminationFight] with Cascade[EliminationFight]

  def fights = eliminationFights.toSeq

}
object EliminationPhase extends EliminationPhase with LongKeyedMetaMapper[EliminationPhase]

