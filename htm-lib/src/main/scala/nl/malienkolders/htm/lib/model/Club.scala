package nl.malienkolders.htm.lib
package model


import net.liftweb._
import mapper._
import common._
import util._
import Helpers._
import scala.xml._
import net.liftweb.json._

case class MarshalledClub(id: Option[Long], code: String, name: String)

class Club extends LongKeyedMapper[Club] with IdPK with CreatedUpdated with Ordered[Club] {
  def getSingleton = Club

  object code extends MappedRequiredPoliteString(this, 8)
  object name extends MappedRequiredPoliteString(this, 128)
  
  def compare(that: Club) =
      this.name.is.compareTo(that.name.is)

  def toMarshalled = MarshalledClub(Some(id.get), code.get, name.get)
  
  def fromMarshalled(m: MarshalledClub) = {
    code(m.code)
    name(m.name)
  }
}

object Club extends Club with LongKeyedMetaMapper[Club]{
  override def dbTableName = "clubs"
}