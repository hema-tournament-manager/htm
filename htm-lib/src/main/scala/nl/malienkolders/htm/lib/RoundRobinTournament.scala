package nl.malienkolders.htm.lib

import scala.collection.immutable.List
import nl.malienkolders.htm.lib.model._

object RoundRobinTournament extends nl.malienkolders.htm.lib.Tournament {

  implicit class PimpedInt(val i: Int) extends AnyVal {
    def isEven = i % 2 == 0
    def isOdd = !isEven
  }

  def roundRobinPairing(nrOfPeople: Int, iteration: Int): List[(Int, Int)] = {
    val pin = 1
    val (topRow: List[Int], bottomRow: List[Int]) = rotate(topRowForCount(nrOfPeople), bottomRowForCount(nrOfPeople), iteration)
    (pin +: topRow).zip(bottomRow.reverse)
  }

  def topRowForCount(nrOfPeople: Int): List[Int] = (2 to ((nrOfPeople + 1) / 2)).toList

  def bottomRowForCount(nrOfPeople: Int): List[Int] = ((((nrOfPeople + 1) / 2) + 1) to nrOfPeople).toList ++ (if (nrOfPeople.isOdd) List(-1) else List())

  def rotate(topRow: List[Int], bottomRow: List[Int], iterations: Int): (List[Int], List[Int]) = iterations match {
    case 0 => (topRow, bottomRow)
    case _ => rotate(
      bottomRow.takeRight(1) ++ topRow.dropRight(1),
      topRow.takeRight(1) ++ bottomRow.dropRight(1),
      iterations - 1)
  }

  def planning(round: Round): List[Pool] = {
    val previous = round.previousRounds
    round.pools.map { pool =>
      val pairings = roundRobinPairing(pool.participants.size, previous.size)
      pairings.foreach {
        case (a, b) if a != -1 && b != -1 =>
          pool.addFight(pool.participants(a - 1), pool.participants(b - 1))
        case _ => // do nothing
      }
      pool.save
      pool
    }.toList
  }

}