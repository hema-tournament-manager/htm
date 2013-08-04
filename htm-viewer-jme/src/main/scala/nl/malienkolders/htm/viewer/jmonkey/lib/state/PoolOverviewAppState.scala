package nl.malienkolders.htm.viewer.jmonkey.lib.state

import com.jme3.font.BitmapText
import nl.malienkolders.htm.viewer.jmonkey.lib.util.TextLabel
import nl.malienkolders.htm.lib.model.MarshalledViewerPool
import java.awt.Color

abstract class PoolOverviewAppState extends TournamentAppState("PoolOverviewRoot") {

  def info(pool: MarshalledViewerPool) {
    updateTextLabels(pool)
  }

  def updateTextLabels(fight: MarshalledViewerPool)

}