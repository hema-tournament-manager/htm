package nl.malienkolders.htm.battle
package model

import net.liftweb._
import mapper._
import common._
import util._
import Helpers._
import scala.xml._
import comet.ViewerServer
import dispatch._
import Http._
import net.liftweb.json._
import lib._

class Participant extends LongKeyedMapper[Participant] with IdPK with CreatedUpdated {
  def getSingleton = Participant

  object name extends MappedPoliteString(this, 128)
  object club extends MappedPoliteString(this, 128)
  object country extends MappedString(this, 2)
}

object Participant extends Participant with LongKeyedMetaMapper[Participant] {
  override def dbTableName = "participants"
}