package nl.malienkolders.htm.lib.model

import net.liftweb.mapper._
import nl.malienkolders.htm.lib.rulesets.Ruleset

sealed class PhaseType(val code: String)
case object PoolType extends PhaseType("P")
case object EliminationType extends PhaseType("E")
case object FreeStyleType extends PhaseType("F")

trait Phase[P <: Phase[P]] extends LongKeyedMapper[P] with IdPK with OneToMany[Long, P] {

  self: P =>

  object order extends MappedLong(this)

  object name extends MappedString(this, 128)

  object tournament extends MappedLongForeignKey(this, Tournament)

  object inUse extends MappedBoolean(this)

  object timeLimitOfFight extends MappedLong(this)
  object breakInFightAt extends MappedLong(this)
  object exchangeLimit extends MappedInt(this)

  object breakDuration extends MappedLong(this)
  object timeBetweenFights extends MappedLong(this)

  object ruleset extends MappedString(this, 64)

  def previousPhase = {
    tournament.foreign.get.phases.find(_.order.is == order.is - 1)
  }

  def previousPhases: List[Phase[_]] = tournament.foreign.get.phases.filter(_.order.is < order.is).toList

  def fights: Seq[Fight[_, _]]

  def rulesetImpl = Ruleset(ruleset.get)
  
  def phaseType: PhaseType

}

class PoolPhase extends Phase[PoolPhase] {

  def getSingleton = PoolPhase

  object pools extends MappedOneToMany(Pool, Pool.phase, OrderBy(Pool.order, Ascending)) with Owned[Pool] with Cascade[Pool]

  // the pool phase is finished when all pools are finished
  def finished_? = pools.map(_.finished_?).forall(x => x)

  def addPool: Pool = {
    val newPool = Pool.create(tournament.obj.get).order(pools.size + 1)
    pools += newPool
    newPool
  }

  def pool(number: Int): Pool = {
    while (pools.size < number) {
      addPool
    }
    pools(number - 1)
  }

  def fights = pools.flatMap(_.fights)
  
  val phaseType = PoolType

}
object PoolPhase extends PoolPhase with LongKeyedMetaMapper[PoolPhase]

class EliminationPhase extends Phase[EliminationPhase] {

  def getSingleton = EliminationPhase

  object eliminationFights extends MappedOneToMany(EliminationFight, EliminationFight.phase, OrderBy(EliminationFight.id, Ascending)) with Owned[EliminationFight] with Cascade[EliminationFight]

  def fights = eliminationFights.toSeq
  
  val phaseType = EliminationType


}
object EliminationPhase extends EliminationPhase with LongKeyedMetaMapper[EliminationPhase]

class FreeStylePhase extends Phase[FreeStylePhase] {

  def getSingleton = FreeStylePhase

  object freeStyleFights extends MappedOneToMany(FreeStyleFight, FreeStyleFight.phase, OrderBy(FreeStyleFight.id, Ascending)) with Owned[FreeStyleFight] with Cascade[FreeStyleFight]

  def compareFights(left: FreeStyleFight, right: FreeStyleFight) = if (left.round.get == right.round.get) {
    left.fightNr.get < right.fightNr.get
  } else {
    left.round.get < right.round.get
  }

  def fights = freeStyleFights.sortWith(compareFights).toSeq

  def lastRound = freeStyleFights.foldLeft(0l) { case (acc, fight) => acc.max(fight.round.get) }
  
  val phaseType = FreeStyleType

}

object FreeStylePhase extends FreeStylePhase with LongKeyedMetaMapper[FreeStylePhase]