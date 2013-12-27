package nl.malienkolders.htm.lib.model

import net.liftweb.mapper._

trait Phase[P <: Phase[P]] extends LongKeyedMapper[P] {

  self: P =>

  object order extends MappedLong(this)

  object name extends MappedString(this, 128)

  object tournament extends MappedLongForeignKey(this, Tournament)

}

class PoolPhase extends Phase[PoolPhase] with IdPK {

  def getSingleton = PoolPhase

}
object PoolPhase extends PoolPhase with LongKeyedMetaMapper[PoolPhase]

class EliminationPhase extends Phase[EliminationPhase] with IdPK {

  def getSingleton = EliminationPhase

}
object EliminationPhase extends EliminationPhase with LongKeyedMetaMapper[EliminationPhase]

