package nl.malienkolders.htm.admin
package model

import net.liftweb._
import mapper._
import common._
import util._
import Helpers._
import scala.xml._
import dispatch._
import Http._
import net.liftweb.json._

class ParticipantNameMapping extends LongKeyedMapper[ParticipantNameMapping] with IdPK with CreatedUpdated {
  def getSingleton = ParticipantNameMapping

  object externalId extends MappedString(this, 8)
  object shortName extends MappedPoliteString(this, 64)
}

object ParticipantNameMapping extends ParticipantNameMapping with LongKeyedMetaMapper[ParticipantNameMapping] with CRUDify[Long, ParticipantNameMapping] {
  override def dbTableName = "participantNameMappings"
}