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

class Score extends LongKeyedMapper[Score] with IdPK with CreatedUpdated with Ordered[Score] {
  def getSingleton = Score

  object fight extends MappedLongForeignKey(this, Fight)
  object timeInFight extends MappedLong(this)
  object timeInWorld extends MappedLong(this)
  object pointsRed extends MappedInt(this)
  object pointsBlue extends MappedInt(this)
  object cleanHitsRed extends MappedInt(this)
  object cleanHitsBlue extends MappedInt(this)
  object afterblowsRed extends MappedInt(this)
  object afterblowsBlue extends MappedInt(this)
  object doubles extends MappedInt(this)
  object exchanges extends MappedInt(this)
  object scoreType extends MappedString(this, 64)

  def compare(that: Score) = (this.timeInFight.is - that.timeInFight.is) match {
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

object Score extends Score with LongKeyedMetaMapper[Score] with CRUDify[Long, Score] {
  override def dbTableName = "scores"
}