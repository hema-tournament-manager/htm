package nl.malienkolders.htm.lib
package model

import net.liftweb._
import mapper._
import common._
import util._
import Helpers._
import scala.xml._
import net.liftweb.json._

case class MarshalledParticipant(id: Long, externalId: String, name: String, shortName: String, club: String, clubCode: String, country: String, q1: Boolean, q2: Boolean, q3: Boolean)

class Participant extends LongKeyedMapper[Participant] with CreatedUpdated with ManyToMany {
  def getSingleton = Participant

  def primaryKeyField = id
  object id extends MappedLongIndex(this)
  object externalId extends MappedString(this, 8)
  object name extends MappedPoliteString(this, 128)
  object shortName extends MappedPoliteString(this, 64)
  object club extends MappedPoliteString(this, 128)
  object clubCode extends MappedPoliteString(this, 6)
  object country extends MappedLongForeignKey(this, Country)
  object isStarFighter extends MappedBoolean(this)
  object isPresent extends MappedBoolean(this)
  object isEquipmentChecked extends MappedBoolean(this)
  object isRankingCheck1 extends MappedBoolean(this)
  object isRankingCheck2 extends MappedBoolean(this)
  object isRankingCheck3 extends MappedBoolean(this)

  object tournaments extends MappedManyToMany(TournamentParticipants, TournamentParticipants.participant, TournamentParticipants.tournament, Tournament)

  def initialRanking = List(isRankingCheck1.is, isRankingCheck2.is, isRankingCheck3.is).count(b => b)

  def toMarshalled = MarshalledParticipant(id.is, externalId.is, name.is, shortName.is, club.is, clubCode.is, country.obj.get.code2.is, isRankingCheck1.is, isRankingCheck2.is, isRankingCheck3.is)
}

object Participant extends Participant with LongKeyedMetaMapper[Participant] with CRUDify[Long, Participant] {
  override def dbTableName = "participants"
}