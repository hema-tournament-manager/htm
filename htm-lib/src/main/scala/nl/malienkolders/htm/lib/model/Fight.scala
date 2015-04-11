package nl.malienkolders.htm.lib
package model

import net.liftweb._
import mapper._
import common._
import util._
import Helpers._
import scala.xml._

case class FightId(phase: String, id: Long)

sealed abstract class Side(serialized: String)
case object Left extends Side("left")
case object Right extends Side("right")
case object Both extends Side("both")
case object Neither extends Side("neither")

trait Fight[F <: Fight[F, S], S <: Score[S, F]] extends LongKeyedMapper[F] with IdPK with FightToScore[F, S] {

  self: F =>

  object name extends MappedString(this, 128)
  object inProgress extends MappedBoolean(this)
  object fighterAFuture extends MappedString(this, 16)
  def fighterAFuture(f: Fighter): F = fighterAFuture(f.format)
  def fighterAFuture(p: Participant): F = fighterAFuture(SpecificFighter(Some(p)))
  object fighterBFuture extends MappedString(this, 16)
  def fighterBFuture(f: Fighter): F = fighterBFuture(f.format)
  def fighterBFuture(p: Participant): F = fighterBFuture(SpecificFighter(Some(p)))
  object timeStart extends MappedLong(this)
  object timeStop extends MappedLong(this)
  object netDuration extends MappedLong(this)
  object cancelled extends MappedBoolean(this) {
    override val defaultValue = false
  }

  def phaseType: PhaseType
  def phase: MappedLongForeignKey[_, _ <: Phase[_]]
  def tournament = phase.foreign.get.tournament.foreign.get
  def scheduled: MappedLongForeignKey[_, _ <: ScheduledFight[_]]

  def started_? = timeStart.is > 0 || inProgress.is
  def finished_? = cancelled.is || timeStop.is > 0
  def grossDuration = timeStop.is - timeStart.is

  def createScore: S = {
    scoreMeta.create
  }

  def addScore(score: Any) = {
    scores += score.asInstanceOf[S]
    score
  }

  def addScore = {
    val score = scoreMeta.create
    scores += score
    score
  }

  def mapScores[A](map: Score[_, _] => A): Seq[A] = scores.map(map)

  def currentScore = scores.foldLeft(TotalScore(0, 0, 0, 0, 0, 0, 0, 0)) { (sum, score) =>
    TotalScore(
      sum.red + score.pointsRed.get,
      sum.redAfter + score.afterblowsRed.get,
      sum.blue + score.pointsBlue.get,
      sum.blueAfter + score.afterblowsBlue.get,
      sum.double + score.doubles.get,
      sum.exchangeCount + score.exchanges.get,
      sum.cleanHitsRed + score.cleanHitsRed.get,
      sum.cleanHitsBlue + score.cleanHitsBlue.get)
  }

  def inFight_?(p: Participant) = (for {
    a <- fighterA.participant
    b <- fighterB.participant
  } yield a.id.is == p.id.is || b.id.is == p.id.is) getOrElse false

  def fighterA: Fighter = Fighter.parse(fighterAFuture.get)
  def fighterB: Fighter = Fighter.parse(fighterBFuture.get)

  def winner = cancelled.get match {
    case true =>
      // cancelled fights cannot be won
      None
    case false =>
      currentScore match {
        case TotalScore(a, _, b, _, _, _, _, _) if a > b => fighterA.participant
        case TotalScore(a, _, b, _, _, _, _, _) if a < b => fighterB.participant
        case _ => None
      }
  }

  def loser = cancelled.get match {
    case true =>
      // cancelled fights cannot be lost
      None
    case false =>
      currentScore match {
        case TotalScore(a, _, b, _, _, _, _, _) if a < b => fighterA.participant
        case TotalScore(a, _, b, _, _, _, _, _) if a > b => fighterB.participant
        case _ => None
      }
  }

  def opponent(fighter: Participant) = (for {
    a <- fighterA.participant
    b <- fighterB.participant
  } yield {
    if (a.id.get == fighter.id.get) {
      Some(b)
    } else if (b.id.get == fighter.id.get) {
      Some(a)
    } else {
      None
    }
  }).getOrElse(None)

  def shortLabel = fighterA.toString + " vs " + fighterB.toString



  def schedule(time: Long, duration: Long): ScheduledFight[_]

  def sameFighters(other: Fight[_, _]) = {
    val myFighters = fighterA :: fighterB :: Nil
    val theirFighters = other.fighterA :: other.fighterB :: Nil
    myFighters.filterNot(my => theirFighters.exists(_.sameAs(my))).isEmpty
  }
}

object FightHelper {
  def dao(phaseType: PhaseType): LongKeyedMetaMapper[_ <: Fight[_, _]] = phaseType match {
    case PoolType => PoolFight
    case EliminationType => EliminationFight
    case FreeStyleType => FreeStyleFight
    case _ => PoolFight
  }

  def dao(phaseType: String): LongKeyedMetaMapper[_ <: Fight[_, _]] = phaseType match {
    case PoolType.code => PoolFight
    case EliminationType.code => EliminationFight
    case FreeStyleType.code => FreeStyleFight
    case _ => PoolFight
  }
}


class PoolFight extends Fight[PoolFight, PoolFightScore] {
  def getSingleton = PoolFight

  def scoreMeta = PoolFightScore

  object pool extends MappedLongForeignKey(this, Pool)
  object order extends MappedLong(this)
  object scheduled extends MappedLongForeignKey(this, ScheduledPoolFight)

  def phase = pool.foreign.get.phase
  val phaseType = PoolType

  def schedule(time: Long, duration: Long) = {
    val sf = ScheduledPoolFight.create.fight(this).time(time).duration(duration)
    scheduled(sf)
    sf
  }
}

object PoolFight extends PoolFight with LongKeyedMetaMapper[PoolFight] {
  override def delete_! = {
    scheduled.foreign.foreach(_.delete_!)
    super.delete_!
  }
}

class EliminationFight extends Fight[EliminationFight, EliminationFightScore] {
  def getSingleton = EliminationFight

  def scoreMeta = EliminationFightScore

  object phase extends MappedLongForeignKey(this, EliminationPhase)
  val phaseType = EliminationType

  object round extends MappedLong(this)
  object scheduled extends MappedLongForeignKey(this, ScheduledEliminationFight)

  def schedule(time: Long, duration: Long) = {
    val sf = ScheduledEliminationFight.create.fight(this).time(time).duration(duration)
    scheduled(sf)
    sf
  }
}
object EliminationFight extends EliminationFight with LongKeyedMetaMapper[EliminationFight] {
  override def delete_! = {
    scheduled.foreign.foreach(_.delete_!)
    super.delete_!
  }
}

class FreeStyleFight extends Fight[FreeStyleFight, FreeStyleFightScore] {
  def getSingleton = FreeStyleFight

  def scoreMeta = FreeStyleFightScore

  object phase extends MappedLongForeignKey(this, FreeStylePhase)

  val phaseType = FreeStyleType

  object round extends MappedLong(this)

  object fightNr extends MappedLong(this)

  object scheduled extends MappedLongForeignKey(this, ScheduledFreeStyleFight)

  def schedule(time: Long, duration: Long) = {
    val sf = ScheduledFreeStyleFight.create.fight(this).time(time).duration(duration)
    scheduled(sf)
    sf
  }
}

object FreeStyleFight extends FreeStyleFight with LongKeyedMetaMapper[FreeStyleFight]