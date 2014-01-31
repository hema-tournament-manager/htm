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

case class GeneratePoolPhase(tournament: Tournament) {

  def generatePoolFights() = {
    val ruleset = tournament.poolPhase.rulesetImpl
    for (pool <- tournament.poolPhase.pools) {
      val planned = ruleset.planning(pool)

      // merge with fights that already exist in this pool
      val merged = planned.map(plannedFight => pool.fights.find(existingFight => existingFight.sameFighters(plannedFight)).getOrElse(plannedFight))
      // renumber the merged fights
      merged.zipWithIndex.foreach { case (f, i) => f.order(i + 1).name(s"Pool ${pool.poolName}, Fight ${i + 1}") }

      pool.fights.clear
      pool.fights ++= merged

      pool.saveMe()
    }
  }

  def generate(numberOfPools: Int): Unit = {
    if (numberOfPools <= 0) {
      S.error("Pool count should be at least 1")
      return ;
    }
    def fill(pts: List[Participant], ps: Seq[Pool]): Unit = pts match {
      case Nil => Unit
      case pt :: pts =>
        ps.head.participants += pt
        fill(pts, ps.tail :+ ps.head)
    }
    tournament.poolPhase.pools.clear;
    tournament.poolPhase.pools ++= (for (i <- 1 to numberOfPools) yield { Pool.create(tournament).order(i) })

    val tournamentSubscriptions = tournament.subscriptions.sortBy(_.experience.is).reverse
    fill(tournamentSubscriptions.map(_.participant.foreign.get).toList, tournament.poolPhase.pools)
    tournament.poolPhase.save
    generatePoolFights()
  }

}