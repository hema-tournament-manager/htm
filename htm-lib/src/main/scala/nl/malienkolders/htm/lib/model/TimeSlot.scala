package nl.malienkolders.htm.lib.model

import net.liftweb._
import mapper._

class Day extends LongKeyedMapper[Day] with IdPK with OneToMany[Long, Day] {
  def getSingleton = Day

  object date extends MappedLong(this)
  object timeslots extends MappedOneToMany(ArenaTimeSlot, ArenaTimeSlot.day, OrderBy(ArenaTimeSlot.from, Ascending))
}
object Day extends Day with LongKeyedMetaMapper[Day] {
  override def dbIndexes = UniqueIndex(date) :: super.dbIndexes
}

class ArenaTimeSlot extends LongKeyedMapper[ArenaTimeSlot] with IdPK with OneToMany[Long, ArenaTimeSlot] {
  def getSingleton = ArenaTimeSlot
  object day extends MappedLongForeignKey(this, Day)
  object arena extends MappedLongForeignKey(this, Arena)
  object name extends MappedString(this, 128)
  object from extends MappedLong(this)
  object to extends MappedLong(this)
  object fightingTime extends MappedBoolean(this)

  object fights extends MappedOneToManyBase[ScheduledFight[_]]({ () =>
    ScheduledPoolFight.findAll(By(ScheduledPoolFight.timeslot, this)) ++ ScheduledEliminationFight.findAll(By(ScheduledEliminationFight.timeslot, this)).sortBy(_.time.is)
  },
    { f: ScheduledFight[_] => f.timeslot.asInstanceOf[MappedForeignKey[Long, _, ArenaTimeSlot]] }) with Owned[ScheduledFight[_]] with Cascade[ScheduledFight[_]]

}
object ArenaTimeSlot extends ArenaTimeSlot with LongKeyedMetaMapper[ArenaTimeSlot]