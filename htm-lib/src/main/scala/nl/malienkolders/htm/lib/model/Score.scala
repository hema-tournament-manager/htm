package nl.malienkolders.htm.lib
package model

import net.liftweb._
import mapper._
import common._
import util._
import Helpers._
import scala.xml._
import net.liftweb.json._

case class MarshalledScore(
  timeInFight: Long,
  timeInWorld: Long,
  pointsRed: Int,
  pointsBlue: Int,
  afterblowsRed: Int,
  afterblowsBlue: Int,
  cleanHitsRed: Int,
  cleanHitsBlue: Int,
  doubles: Int,
  exchanges: Int,
  scoreType: String)

case class TotalScore(
  red: Int,
  redAfter: Int,
  blue: Int,
  blueAfter: Int,
  double: Int,
  exchangeCount: Int)

trait Score[S <: Score[S, F], F <: Fight[F, S]] extends LongKeyedMapper[S] {
  self: S =>

  object fight extends MappedLongForeignKey(this, fightMeta)

  def fightMeta: F with LongKeyedMetaMapper[F]

  //object fight extends MappedLongForeignKey(this, Fight)
  object timeInFight extends MappedLong[S](this)
  object timeInWorld extends MappedLong[S](this)
  object pointsRed extends MappedInt[S](this)
  object pointsBlue extends MappedInt[S](this)
  object cleanHitsRed extends MappedInt[S](this)
  object cleanHitsBlue extends MappedInt[S](this)
  object afterblowsRed extends MappedInt[S](this)
  object afterblowsBlue extends MappedInt[S](this)
  object doubles extends MappedInt[S](this)
  object exchanges extends MappedInt[S](this)
  object scoreType extends MappedString[S](this, 64)

  def compare(that: Score[_, _]) = (this.timeInFight.is - that.timeInFight.is) match {
    case d if d > 0 => 1
    case d if d < 0 => -1
    case _ => 0
  }

  def toMarshalled =
    MarshalledScore(timeInFight.is, timeInWorld.is, pointsRed.is, pointsBlue.is,
      cleanHitsRed.is, cleanHitsBlue.is, afterblowsRed.is, afterblowsBlue.is,
      doubles.is, exchanges.is, scoreType.is)

  def fromMarshalled(m: MarshalledScore) = {
    timeInFight(m.timeInFight)
    timeInWorld(m.timeInWorld)
    pointsRed(m.pointsRed)
    pointsBlue(m.pointsBlue)
    cleanHitsRed(m.cleanHitsRed)
    cleanHitsBlue(m.cleanHitsBlue)
    afterblowsRed(m.afterblowsRed)
    afterblowsBlue(m.afterblowsBlue)
    doubles(m.doubles)
    exchanges(m.exchanges)
    scoreType(m.scoreType)
    this
  }
}

class PoolFightScore extends Score[PoolFightScore, PoolFight] with IdPK {
  def getSingleton = PoolFightScore
  def fightMeta = PoolFight
}
object PoolFightScore extends PoolFightScore with LongKeyedMetaMapper[PoolFightScore]

class EliminationFightScore extends Score[EliminationFightScore, EliminationFight] with IdPK {
  def getSingleton = EliminationFightScore
  def fightMeta = EliminationFight
}
object EliminationFightScore extends EliminationFightScore with LongKeyedMetaMapper[EliminationFightScore]

trait FightToScore[F <: Fight[F, S], S <: Score[S, F]] extends OneToMany[Long, F] {
  self: F =>

  object scores extends MappedOneToMany(scoreMeta, scoreMeta.fight)

  def scoreMeta: LongKeyedMetaMapper[S] with S
}