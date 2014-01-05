package nl.malienkolders.htm.lib
package model

import net.liftweb._
import mapper._
import common._
import util._
import Helpers._
import scala.xml._
import net.liftweb.json._

case class MarshalledScheduledFightSummary(time: Long, fight: MarshalledFightSummary)

trait ScheduledFight[F <: ScheduledFight[F]] extends LongKeyedMapper[F] with Ordered[F] {
  self: F =>

  object arena extends MappedLongForeignKey(this, Arena)
  def fight: MappedLongForeignKey[F, _ <: Fight[_, _]] 
  object time extends MappedLong(this)
  
  def compare(that: F) = this.time.is.compareTo(that.time.is)
  
  def toMarshalledSummary = MarshalledScheduledFightSummary(time.is, fight.foreign.get.toMarshalledSummary)
}

class ScheduledPoolFight extends ScheduledFight[ScheduledPoolFight] with IdPK {
  def getSingleton = ScheduledPoolFight

  object fight extends MappedLongForeignKey(this, PoolFight)
}

object ScheduledPoolFight extends ScheduledPoolFight with LongKeyedMetaMapper[ScheduledPoolFight] {
  override def dbTableName = "scheduled_pool_fight"
}

class ScheduledEliminationFight extends ScheduledFight[ScheduledEliminationFight] with IdPK {
  def getSingleton = ScheduledEliminationFight

  object fight extends MappedLongForeignKey(this, EliminationFight)
}

object ScheduledEliminationFight extends ScheduledEliminationFight with LongKeyedMetaMapper[ScheduledEliminationFight] {
  override def dbTableName = "scheduled_elimination_fight"
}