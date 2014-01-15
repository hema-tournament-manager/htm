package nl.malienkolders.htm.admin.lib.importer

import net.liftweb.json._

case class Event(name: String, days: List[Day], arenas: Int, tournaments: List[Tournament])

case class Day(date: String, timeslots: List[Timeslot])

case class Timeslot(name: String, from: String, to: String)

case class Tournament(id: String, name: String, mnemonic: String, poolPhase: PoolPhase, eliminationPhase: EliminationPhase)

case class PoolPhase(calculatePools: PoolCalculation)

case class PoolCalculation(size: List[Int])

case class EliminationPhase(fighters: Int, pick: EliminationPick)

case class EliminationPick(top: Int)

object Event {
  implicit val formats = DefaultFormats

  def parse(data: String): Event = JsonParser.parse(data).extract[Event]
}