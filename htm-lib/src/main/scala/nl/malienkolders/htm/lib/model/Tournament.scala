package nl.malienkolders.htm.lib.model

import net.liftweb._
import mapper._

case class MarshalledTournamentSummary(id: Long, name: String, identifier: String, rapier: Boolean)
case class MarshalledTournament(id: Long, name: String, identifier: String, participants: List[Long], rounds: List[Long])

class Tournament extends LongKeyedMapper[Tournament] with OneToMany[Long, Tournament] with Ordered[Tournament] {

  def getSingleton = Tournament

  def primaryKeyField = id
  object id extends MappedLongIndex(this)
  object name extends MappedString(this, 32)
  object identifier extends MappedString(this, 32)
  object rounds extends MappedOneToMany(Round, Round.tournament, OrderBy(Round.order, Ascending)) with Owned[Round] with Cascade[Round]
  object defaultArena extends MappedLongForeignKey(this, Arena)
  object subscriptions extends MappedOneToMany(TournamentParticipants, TournamentParticipants.tournament, OrderBy(TournamentParticipants.fighterNumber, Ascending))
  def participants = subscriptions.map(_.tournament.obj.get)

  def rapier_? = name.is.toLowerCase().contains("rapier")

  def nextFighterNumber: Int = {
    subscriptions.map(_.fighterNumber.get).max + 1
  }

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
  object fighterNumber extends MappedInt(this)
  object primary extends MappedBoolean(this)
  object experience extends MappedInt(this)
  object gearChecked extends MappedBoolean(this)
}
object TournamentParticipants extends TournamentParticipants with LongKeyedMetaMapper[TournamentParticipants]