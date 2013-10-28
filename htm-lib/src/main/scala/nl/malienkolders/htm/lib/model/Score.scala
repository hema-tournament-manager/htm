package nl.malienkolders.htm.lib
package model

import net.liftweb._
import mapper._
import common._
import util._
import Helpers._
import scala.xml._
import net.liftweb.json._

case class MarshalledScore(timeInFight: Long, timeInWorld: Long, diffA: Int, diffB: Int, diffAAfterblow: Int, diffBAfterblow: Int, diffDouble: Int, scoreType: String, isSpecial: Boolean, diffExchange: Int)
case class TotalScore(
  a: Int,
  aAfter: Int,
  b: Int,
  bAfter: Int,
  double: Int,
  specialHitsA: Int,
  specialHitsB: Int,
  exchangeCount: Int)

class Score extends LongKeyedMapper[Score] with IdPK with CreatedUpdated with Ordered[Score] {
  def getSingleton = Score

  object fight extends MappedLongForeignKey(this, Fight)
  object timeInFight extends MappedLong(this)
  object timeInWorld extends MappedLong(this)
  object diffA extends MappedInt(this)
  object diffB extends MappedInt(this)
  object diffAAfterblow extends MappedInt(this)
  object diffBAfterblow extends MappedInt(this)
  object diffDouble extends MappedInt(this)
  object isSpecial extends MappedBoolean(this)
  object diffExchange extends MappedInt(this)
  object scoreType extends MappedString(this, 64)

  def compare(that: Score) = (this.timeInFight.is - that.timeInFight.is) match {
    case d if d > 0 => 1
    case d if d < 0 => -1
    case _ => 0
  }

  def toMarshalled = MarshalledScore(timeInFight.is, timeInWorld.is, diffA.is, diffB.is, diffAAfterblow.is, diffBAfterblow.is, diffDouble.is, scoreType.is, isSpecial.is, diffExchange.is)
  def fromMarshalled(m: MarshalledScore) = {
    timeInFight(m.timeInFight)
    timeInWorld(m.timeInWorld)
    diffA(m.diffA)
    diffB(m.diffB)
    diffAAfterblow(m.diffAAfterblow)
    diffBAfterblow(m.diffBAfterblow)
    diffDouble(m.diffDouble)
    scoreType(m.scoreType)
    isSpecial(m.isSpecial)
    diffExchange(m.diffExchange)
    this
  }
}

object Score extends Score with LongKeyedMetaMapper[Score] with CRUDify[Long, Score] {
  override def dbTableName = "scores"
}