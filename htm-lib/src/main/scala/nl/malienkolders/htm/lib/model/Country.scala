package nl.malienkolders.htm.lib
package model

import net.liftweb._
import mapper._
import common._
import util._
import Helpers._
import scala.xml._
import net.liftweb.json._

case class MarshalledCountry(code2: String, name: String)

class Country extends LongKeyedMapper[Country] with IdPK with CreatedUpdated with Ordered[Country] {
  def getSingleton = Country

  object code2 extends MappedString(this, 2)
  object code3 extends MappedString(this, 3)
  object name extends MappedString(this, 64)
  object hasFlag extends MappedBoolean(this)
  object hasViewerFlag extends MappedBoolean(this)

  def compare(that: Country) =
    if (this.code2.is == "")
      1
    else
      this.name.is.compareTo(that.name.is)

  def toMarshalled = MarshalledCountry(code2.get, name.get)
}

object Country extends Country with LongKeyedMetaMapper[Country] {
  override def dbTableName = "countries"
}