package nl.malienkolders.htm.admin.lib

import nl.malienkolders.htm.lib.model._

object TournamentGenerators {

  implicit class EliminationPhaseGenerator(t: Tournament) {

    def generateElimination(n: Int) = {
      val nthFinals = Math.pow(2, n.max(1)).toInt

      t.eliminationPhase.eliminationFights.clear()

      val round1 = for (i <- 1 to nthFinals) yield EliminationFight.create
        .round(1)
        .name(s"1/$nthFinals Finals, Fight $i")
        .fighterAFuture(SpecificFighter(None).format)
        .fighterBFuture(SpecificFighter(None).format)

      t.eliminationPhase.eliminationFights ++= round1
      t.save()

      generateNextRound(round1.toList, 2)

      generateFinals()

      def generateNextRound(previous: List[EliminationFight], roundNumber: Int): Unit =
        previous.size match {
          case n if n < 4 => Nil
          case n =>
            val next = (for (i <- 0 to (n / 2 - 1)) yield EliminationFight.create
              .round(roundNumber)
              .name("1/" + (n / 2) + " Finals, Fight " + (i + 1))
              .fighterAFuture(Winner(previous(i * 2)).format)
              .fighterBFuture(Winner(previous(i * 2 + 1)).format)).toList

            t.eliminationPhase.eliminationFights ++= next
            // we have to save the fights to get their id's
            t.save()

            generateNextRound(next, roundNumber + 1)
        }

      def generateFinals() = {
        val semiFinals = t.eliminationPhase.eliminationFights.takeRight(2)

        // add fights if necessary
        t.finalsPhase.eliminationFights ++= (for (i <- t.finalsPhase.eliminationFights.size to 1) yield EliminationFight.create)

        t.finalsPhase.eliminationFights.head
          .round(1)
          .name("3rd Place")
          .fighterAFuture(Loser(semiFinals(0)).format)
          .fighterBFuture(Loser(semiFinals(1)).format)
        t.finalsPhase.eliminationFights.last
          .round(2)
          .name("1st Place")
          .fighterAFuture(Winner(semiFinals(0)).format)
          .fighterBFuture(Winner(semiFinals(1)).format)
        t.save()
      }
    }
  }

  implicit class PoolPhaseGenerator(t: Tournament) {
    def generatePools(numberOfPools: Int): Unit = {

      def generatePoolFights() = {
        val ruleset = t.poolPhase.rulesetImpl
        for (pool <- t.poolPhase.pools) {
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

      def fill(pts: List[Participant], ps: Seq[Pool]): Unit = pts match {
        case Nil => Unit
        case pt :: pts =>
          ps.head.participants += pt
          fill(pts, ps.tail :+ ps.head)
      }
      t.poolPhase.pools.clear;
      t.poolPhase.pools ++= (for (i <- 1 to numberOfPools) yield { Pool.create(t).order(i) })

      val tournamentSubscriptions = t.subscriptions.sortBy(_.experience.is).reverse
      fill(tournamentSubscriptions.map(_.participant.foreign.get).toList, t.poolPhase.pools)
      t.poolPhase.save
      generatePoolFights()
    }

  }

}