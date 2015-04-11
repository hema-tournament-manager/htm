package nl.malienkolders.htm.lib
package model

import net.liftweb._
import mapper._
import common._
import util._
import Helpers._
import scala.xml._
import net.liftweb.json._


class Participant extends LongKeyedMapper[Participant] with CreatedUpdated with OneToMany[Long, Participant] {
  def getSingleton = Participant

  def primaryKeyField = id
  object id extends MappedLongIndex(this)
  object externalId extends MappedString(this, 8)
  object name extends MappedRequiredPoliteString(this, 128)
  object shortName extends MappedPoliteString(this, 64)
  object club extends MappedPoliteString(this, 128)
  object clubCode extends MappedPoliteString(this, 16)
  object country extends MappedLongForeignKey(this, Country)
  object isStarFighter extends MappedBoolean(this)
  object isPresent extends MappedBoolean(this)
  object tshirt extends MappedPoliteString(this, 32)
  object age extends MappedInt(this)
  object height extends MappedInt(this)
  object weight extends MappedInt(this)
  object previousWins extends MappedTextarea(this, 1024)

  def tournaments = subscriptions.map(_.tournament.obj.get)
  object subscriptions extends MappedOneToMany(TournamentParticipant, TournamentParticipant.participant, OrderBy(TournamentParticipant.id, Ascending))

  def subscription(t: Tournament): Option[TournamentParticipant] = subscriptions.find(_.tournament.get == t.id.get)
  def subscription(p: Phase[_]): Option[TournamentParticipant] = subscription(p.tournament.obj.get)

  def initialRanking(t: Tournament): Int = subscription(t).map(_.experience.get).getOrElse(-1)
  def initialRanking(p: Phase[_]): Int = initialRanking(p.tournament.obj.get)

  def poolForTournament(t: Tournament): Option[Pool] = {
    t.poolPhase.pools.find(_.participants.exists(_.id.is == id.is))
  }
  

}

object Participant extends Participant with LongKeyedMetaMapper[Participant] with CRUDify[Long, Participant] {
  override def dbTableName = "participants"
}
