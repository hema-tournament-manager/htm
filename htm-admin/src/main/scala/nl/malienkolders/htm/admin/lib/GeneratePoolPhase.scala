package nl.malienkolders.htm.admin.lib

import net.liftweb.http.js.JsCmds.RedirectTo
import net.liftweb._
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.sitemap._
import Loc._
import nl.malienkolders.htm.lib.model._
import nl.malienkolders.htm.lib.util.Helpers._
import net.liftweb.http.js.JsCmds.{ Reload, RedirectTo }

class GeneratePoolPhase(tournament: Tournament) {

  val tournamentSubscriptions = tournament.subscriptions.sortBy(_.experience.is).reverse
  var poolCount: Int = 0;

  private def generatePoolFights() = {
    val ruleset = tournament.poolPhase.rulesetImpl
    for (pool <- tournament.poolPhase.pools) {
      val planned = ruleset.planning(pool)
      // remove all fights that already exist in the pool
      val newlyPlanned = planned.filterNot(plannedFight => pool.fights.exists(existingFight => existingFight.sameFighters(plannedFight)))

      pool.fights ++= newlyPlanned

      // renumber the merged fights
      pool.fights.sortBy(_.order.is).zipWithIndex.foreach { case (f, i) => f.order(i + 1).name("Pool " + pool.poolName + ", Fight " + (i + 1)) }

      pool.saveMe()
    }
  }

  def generate(): Unit = {
    if (poolCount <= 0) {
      S.error("Pool count should be at least 1")
      return ;
    }
    def fill(pts: List[Participant], ps: Seq[Pool]): Unit = pts match {
      case Nil => Unit
      case pt :: pts =>
        ps.head.participants += pt
        fill(pts, if (ps.head.participants.size.isOdd) ps else ps.tail :+ ps.head)
    }
    tournament.poolPhase.pools.clear;
    tournament.poolPhase.pools ++= (for (i <- 1 to poolCount) yield { Pool.create(tournament).order(i) })

    fill(tournamentSubscriptions.map(_.participant.foreign.get).toList, tournament.poolPhase.pools)
    tournament.poolPhase.save
    generatePoolFights()
  }

}