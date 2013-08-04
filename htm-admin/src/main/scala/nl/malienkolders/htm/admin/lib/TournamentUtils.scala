package nl.malienkolders.htm.admin.lib

import nl.malienkolders.htm.lib.model._
import net.liftweb._
import http._

object TournamentUtils {

  abstract class AdvanceSet
  case object All extends AdvanceSet
  case class Selected(participants: List[Participant]) extends AdvanceSet
  case object Winners extends AdvanceSet
  case class Single(participant: Participant) extends AdvanceSet

  def advance(round: Round, who: AdvanceSet) {
    round.previousRound.map { prev =>
      who match {
        case Single(p) =>
          if (round.pools.isEmpty)
            round.pools += Pool.create.order(1)
          round.pools(0).participants += p

        case notSingle =>
          round.pools.clear
          val participants = notSingle match {
            case All =>
              prev.pools.map(_.participants.toList).toList

            case Selected(ps) =>
              ps :: Nil

            case Winners =>
              val winners = prev.pools.flatMap(_.fights.map(_.winner)).filter(_.isDefined).map(_.get).toList
              winners :: Nil
          }
          participants.zipWithIndex.foreach {
            case (ps, i) =>
              val newPool = Pool.create.order(i + 1)
              newPool.participants ++= ps
              round.pools += newPool
          }
      }
      round.save
    } openOr (S.notice("No previous round found"))
  }

}