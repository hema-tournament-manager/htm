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

class AdminServer extends LongKeyedMapper[AdminServer] with IdPK with CreatedUpdated {
  def getSingleton = AdminServer

  object hostname extends MappedString(this, 128)
  object port extends MappedInt(this)
}

object AdminServer extends AdminServer with LongKeyedMetaMapper[AdminServer] {
  override def dbTableName = "adminservers"
}

