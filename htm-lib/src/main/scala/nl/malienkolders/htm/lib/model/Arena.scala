package nl.malienkolders.htm.lib
package model

import net.liftweb._
import mapper._
import common._
import util._
import Helpers._
import scala.xml._
import net.liftweb.json._


class Arena extends LongKeyedMapper[Arena] with IdPK with CreatedUpdated with Ordered[Arena] with ManyToMany with OneToMany[Long, Arena] {
  def getSingleton = Arena

  object name extends MappedString(this, 64)

  object timeslots extends MappedOneToMany(ArenaTimeSlot, ArenaTimeSlot.arena, OrderBy(ArenaTimeSlot.day, Ascending), OrderBy(ArenaTimeSlot.from, Ascending))

  def fights = timeslots.flatMap(_.fights)

  /**
   * @return List of all the scheduled fights that have an existing fight, sorted by day and time
   */
  def scheduledFights = timeslots.flatMap(_.fights).filter(_.fight.foreign.isDefined).sortBy(sf => (sf.timeslot.foreign.get.day.is, sf.time.is))

  def timeslotByDay = timeslots.groupBy(_.day.foreign.get).toList.sortBy(_._1.date.is)

  object viewers extends MappedManyToMany(ArenaViewers, ArenaViewers.arena, ArenaViewers.viewer, Viewer)

  def compare(that: Arena) = this.name.is.compareTo(that.name.is)

  
}

object Arena extends Arena with LongKeyedMetaMapper[Arena] {
  override def dbTableName = "arenas"
    
  def default: Arena = findAll().headOption.getOrElse(Arena.create.name("Default Arena").saveMe())

}

class ArenaViewers extends LongKeyedMapper[ArenaViewers] with IdPK {
  def getSingleton = ArenaViewers

  object arena extends MappedLongForeignKey(this, Arena)
  object viewer extends MappedLongForeignKey(this, Viewer)
}

object ArenaViewers extends ArenaViewers with LongKeyedMetaMapper[ArenaViewers] {
  override def dbTableName = "arena_viewers"
}