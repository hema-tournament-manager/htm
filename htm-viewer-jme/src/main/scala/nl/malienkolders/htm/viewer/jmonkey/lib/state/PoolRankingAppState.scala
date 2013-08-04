package nl.malienkolders.htm.viewer.jmonkey.lib.state

import nl.malienkolders.htm.lib.model._

abstract class PoolRankingAppState extends TournamentAppState("PoolRankingRoot") {

  def info(pool: MarshalledPoolRanking) {
    updateTextLabels(pool)
  }

  def updateTextLabels(pool: MarshalledPoolRanking)

}