package nl.malienkolders.htm.lib.model

import net.liftweb._
import mapper._

case class MarshalledTournamentSummary(id: Long, name: String, identifier: String, rapier: Boolean)
case class MarshalledTournament(id: Long, name: String, identifier: String, participants: List[Long], rounds: List[Long])

class Tournament extends LongKeyedMapper[Tournament] with OneToMany[Long, Tournament] with ManyToMany with Ordered[Tournament] {

  def getSingleton = Tournament

  def primaryKeyField = id
  object id extends MappedLongIndex(this)
  object name extends MappedString(this, 32)
  object identifier extends MappedString(this, 32)
  object participants extends MappedManyToMany(TournamentParticipants, TournamentParticipants.tournament, TournamentParticipants.participant, Participant)
  object rounds extends MappedOneToMany(Round, Round.tournament, OrderBy(Round.order, Ascending)) with Owned[Round] with Cascade[Round]

  def rapier_? = name.is.toLowerCase().contains("rapier")

  def toMarshalled = MarshalledTournament(id.is, name.is, identifier.is, participants.map(_.id.is).toList, rounds.map(_.id.is).toList)
  def toMarshalledSummary = MarshalledTournamentSummary(id.is, name.is, identifier.is, rapier_?)

  def compare(that: Tournament) = (this.id.is - that.id.is) match {
    case d if d > 0 => 1
    case d if d < 0 => -1
    case _ => 0
  }
}
object Tournament extends Tournament with LongKeyedMetaMapper[Tournament]

class TournamentParticipants extends LongKeyedMapper[TournamentParticipants] with IdPK {
  def getSingleton = TournamentParticipants
  object tournament extends MappedLongForeignKey(this, Tournament)
  object participant extends MappedLongForeignKey(this, Participant)
}
object TournamentParticipants extends TournamentParticipants with LongKeyedMetaMapper[TournamentParticipants]