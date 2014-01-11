package nl.malienkolders.htm.lib.model

import net.liftweb._
import mapper._
import com.github.nscala_time.time.Imports._

class Event extends LongKeyedMapper[Event] with IdPK with OneToMany[Long, Event] {

  def getSingleton = Event

  object name extends MappedString(this, 32)
}
object Event extends Event with LongKeyedMetaMapper[Event] {

  def theOne: Event = findAll().headOption.getOrElse(Event.create.name("My Event " + LocalDate.now.year().get()).saveMe())

}