package nl.malienkolders.htm.lib.model

import net.liftweb._
import mapper._

case class MarshalledTournamentSummary(id: Long, name: String, identifier: String, rapier: Boolean)
case class MarshalledTournament(id: Long, name: String, identifier: String, participants: List[Long])
case class MarshalledTournamentRound(id: Long, finished: Boolean)

class Tournament extends LongKeyedMapper[Tournament] with OneToMany[Long, Tournament] with Ordered[Tournament] {

  def getSingleton = Tournament

  def primaryKeyField = id
  object id extends MappedLongIndex(this)
  object name extends MappedString(this, 64)
  object mnemonic extends MappedString(this, 8)
  object identifier extends MappedString(this, 32)
  object phases extends MappedOneToManyBase[Phase[_]]({ () =>
    PoolPhase.findAll(By(PoolPhase.tournament, this)) ++ EliminationPhase.findAll(By(EliminationPhase.tournament, this)).sortBy(_.order.is)
  },
    { p: Phase[_] => p.tournament.asInstanceOf[MappedForeignKey[Long, _, Tournament]] }) with Owned[Phase[_]] with Cascade[Phase[_]]
  object defaultArena extends MappedLongForeignKey(this, Arena)
  object subscriptions extends MappedOneToMany(TournamentParticipants, TournamentParticipants.tournament, OrderBy(TournamentParticipants.fighterNumber, Ascending))
  def participants = subscriptions.map(_.participant.obj.get)

  def rapier_? = name.is.toLowerCase().contains("rapier")

  def nextFighterNumber: Int = subscriptions.size match {
    case 0 => 1
    case _ => subscriptions.map(_.fighterNumber.get).max + 1
  }

  def startTime = 0

  def toMarshalled = MarshalledTournament(
    id.is,
    name.is,
    identifier.is,
    participants.map(_.id.is).toList)
  def toMarshalledSummary = MarshalledTournamentSummary(id.is, name.is, identifier.is, rapier_?)

  def compare(that: Tournament) = (this.id.is - that.id.is) match {
    case d if d > 0 => 1
    case d if d < 0 => -1
    case _ => 0
  }

  def poolPhase: PoolPhase = phases(0).asInstanceOf[PoolPhase]

  def eliminationPhase: EliminationPhase = phases(1).asInstanceOf[EliminationPhase]

  def finalsPhase: EliminationPhase = phases(2).asInstanceOf[EliminationPhase]
}
object Tournament extends Tournament with LongKeyedMetaMapper[Tournament] {

  override def create = {
    val t = super.create
    t.phases ++= List(PoolPhase.create, EliminationPhase.create, EliminationPhase.create)
    t.poolPhase.name("Pool Phase")
    t.eliminationPhase.name("Elimination Phase")
    t.finalsPhase.name("Finals")
    t
  }
}

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