package nl.malienkolders.htm.lib.model

import net.liftweb._
import mapper._

case class MarshalledTournamentSummary(id: Long, name: String, identifier: String, memo: String)
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
  object subscriptions extends MappedOneToMany(TournamentParticipant, TournamentParticipant.tournament, OrderBy(TournamentParticipant.fighterNumber, Ascending)) with Owned[TournamentParticipant] with Cascade[TournamentParticipant]
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
  def toMarshalledSummary = MarshalledTournamentSummary(id.is, name.is, identifier.is, mnemonic.is)

  def compare(that: Tournament) = (this.id.is - that.id.is) match {
    case d if d > 0 => 1
    case d if d < 0 => -1
    case _ => 0
  }

  def poolPhase: PoolPhase = phases(0).asInstanceOf[PoolPhase]

  def eliminationPhase: EliminationPhase = phases(1).asInstanceOf[EliminationPhase]

  def finalsPhase: EliminationPhase = phases(2).asInstanceOf[EliminationPhase]

  def fights = phases.flatMap(_.fights)

  def removeParticipant(sub: TournamentParticipant) = sub.hasFought match {
    case false =>
      fights.filter(_.inFight_?(sub.participant.foreign.get)).foreach(_.delete_!)
      val p = sub.participant.foreign.get
      p.poolForTournament(sub.tournament.foreign.get).foreach { pool =>
        pool.participants -= p
        pool.save
      }
      subscriptions -= sub
      save()
      sub.delete_!
      true
    case true =>
      false
  }

  def dropParticipantOut(sub: TournamentParticipant) = {
    fights.filter(_.inFight_?(sub.participant.foreign.get)).foreach { f =>
      f.cancelled(true)
      f.scheduled.foreign.foreach(_.delete_!)
      f.save()
    }
    sub.droppedOut(true).save()
  }

  def dropParticipantIn(sub: TournamentParticipant) = {
    fights.filter(_.inFight_?(sub.participant.foreign.get)).foreach { f =>
      // if the opponent in this fight has dropped out, the fight stays cancelled
      val cancelled_? = for {
        opponent <- f.opponent(sub.participant.foreign.get)
        opponentSub <- opponent.subscription(sub.tournament.foreign.get)
      } yield opponentSub.droppedOut.is

      f.cancelled(cancelled_?.getOrElse(false))
      f.save()
    }
    sub.droppedOut(false).save()
  }
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

class TournamentParticipant extends LongKeyedMapper[TournamentParticipant] with IdPK {
  def getSingleton = TournamentParticipant
  object tournament extends MappedLongForeignKey(this, Tournament)
  object participant extends MappedLongForeignKey(this, Participant)
  object fighterNumber extends MappedInt(this)
  object primary extends MappedBoolean(this)
  object experience extends MappedInt(this)
  object gearChecked extends MappedBoolean(this)
  object droppedOut extends MappedBoolean(this) {
    override val defaultValue = false
  }

  import TournamentParticipant._

  private def error(error: SubscriptionError): List[SubscriptionError] = error.field.is match {
    case error.errorValue => List(error)
    case _ => Nil
  }

  def errors: List[SubscriptionError] =
    error(NotPresent(this)) ++ error(GearNotChecked(this)) ++ error(HasDroppedOut(this))

  def hasError: Boolean = !errors.isEmpty

  def hasFought: Boolean = tournament.foreign.get.fights.exists(f => f.finished_? && f.inFight_?(participant.foreign.get))

  def toMarshalled = participant.foreign.get.toMarshalled.copy(
    fighterNumber = Some(fighterNumber.get),
    gearChecked = Some(gearChecked.get),
    pool = Some(participant.foreign.get.poolForTournament(tournament.foreign.get).get.poolName))
}
object TournamentParticipant extends TournamentParticipant with LongKeyedMetaMapper[TournamentParticipant] {
  type ErrorField = MappedBoolean[_ <: LongKeyedMapper[_]]
  sealed class SubscriptionError(val field: ErrorField, val errorValue: Boolean, val caption: String)
  case class NotPresent(s: TournamentParticipant) extends SubscriptionError(s.participant.foreign.get.isPresent, false, "Not present")
  case class GearNotChecked(s: TournamentParticipant) extends SubscriptionError(s.gearChecked, false, "Gear not checked")
  case class HasDroppedOut(s: TournamentParticipant) extends SubscriptionError(s.droppedOut, true, "Dropped out")
}
