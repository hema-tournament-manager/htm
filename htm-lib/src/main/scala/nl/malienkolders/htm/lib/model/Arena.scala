package nl.malienkolders.htm.lib
package model

import net.liftweb._
import mapper._
import common._
import util._
import Helpers._
import scala.xml._
import net.liftweb.json._

case class MarshalledArena(id: Long, name: String)

class Arena extends LongKeyedMapper[Arena] with IdPK with CreatedUpdated with Ordered[Arena] with ManyToMany with OneToMany[Long, Arena] {
  def getSingleton = Arena

  object name extends MappedString(this, 64)

  object fights extends MappedOneToManyBase[ScheduledFight[_]]({ () =>
    ScheduledPoolFight.findAll(By(ScheduledPoolFight.arena, this)) ++ ScheduledEliminationFight.findAll(By(ScheduledEliminationFight.arena, this)).sortBy(_.time.is)
  },
    { f: ScheduledFight[_] => f.arena.asInstanceOf[MappedForeignKey[Long, _, Arena]] }) with Owned[ScheduledFight[_]] with Cascade[ScheduledFight[_]]
  
  object viewers extends MappedManyToMany(ArenaViewers, ArenaViewers.arena, ArenaViewers.viewer, Viewer)

  def compare(that: Arena) = this.name.is.compareTo(that.name.is)

  def toMarshalled = MarshalledArena(id.get, name.get)
}

object Arena extends Arena with LongKeyedMetaMapper[Arena] {
  override def dbTableName = "arenas"
}

class ArenaViewers extends LongKeyedMapper[ArenaViewers] with IdPK {
  def getSingleton = ArenaViewers

  object arena extends MappedLongForeignKey(this, Arena)
  object viewer extends MappedLongForeignKey(this, Viewer)
}

object ArenaViewers extends ArenaViewers with LongKeyedMetaMapper[ArenaViewers] {
  override def dbTableName = "arena_viewers"
}