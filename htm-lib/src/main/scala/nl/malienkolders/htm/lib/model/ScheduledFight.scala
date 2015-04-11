package nl.malienkolders.htm.lib
package model

import net.liftweb._
import mapper._
import common._
import util._
import Helpers._
import scala.xml._
import net.liftweb.json._


trait ScheduledFight[F <: ScheduledFight[F]] extends LongKeyedMapper[F] with SaveMessageBroadcaster[F] {
  self: F =>

  object timeslot extends MappedLongForeignKey(this, ArenaTimeSlot)
  def fight: MappedLongForeignKey[F, _ <: Fight[_, _]]
  object time extends MappedLong(this)
  object duration extends MappedLong(this)

  def previous: Option[ScheduledFight[_]] = {
    val ts = timeslot.foreign.get
    ts.fights.sortBy(_.time.is).reverse.find(_.time.is < time.is)
  }

  def next: Option[ScheduledFight[_]] = {
    val ts = timeslot.foreign.get
    ts.fights.sortBy(_.time.is).find(_.time.is > time.is)
  }

  def compare(that: F) = this.time.is.compareTo(that.time.is)

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

class ScheduledFreeStyleFight extends ScheduledFight[ScheduledFreeStyleFight] with IdPK {
  def getSingleton = ScheduledFreeStyleFight

  object fight extends MappedLongForeignKey(this, FreeStyleFight)
}

object ScheduledFreeStyleFight extends ScheduledFreeStyleFight with LongKeyedMetaMapper[ScheduledFreeStyleFight] {
  override def dbTableName = "scheduled_freestyle_fight"
}