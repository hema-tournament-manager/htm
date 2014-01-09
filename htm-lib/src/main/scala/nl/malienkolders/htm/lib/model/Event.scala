package nl.malienkolders.htm.lib.model

import net.liftweb._
import mapper._

class Event extends LongKeyedMapper[Event] with IdPK with OneToMany[Long, Event] {

  def getSingleton = Event

  object name extends MappedString(this, 32)
}
object Event extends Event with LongKeyedMetaMapper[Event]