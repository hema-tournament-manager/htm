package nl.malienkolders.htm.viewer.jmonkey.lib.state
package spectator

import java.awt.Color
import nl.malienkolders.htm.viewer.jmonkey.lib._
import java.awt.Font
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.awt.Graphics2D
import nl.malienkolders.htm.viewer.jmonkey.lib.util._
import com.jme3._
import texture._
import scene._
import scene.shape._
import material._
import renderer.queue.RenderQueue
import math._
import font._
import nl.malienkolders.htm.lib.HtmHelpers._
import nl.malienkolders.htm.lib.model._
import scala.util.Random
import nl.malienkolders.htm.viewer.jmonkey.SpectatorScreen

object PoolOverviewAppState extends nl.malienkolders.htm.viewer.jmonkey.lib.state.PoolOverviewAppState {

  case class PixelsToUnit(value: Int) {
    def pixels = value * SpectatorScreen.upp
    def px = pixels
  }
  implicit def intToPixelsToUnit(i: Int): PixelsToUnit = PixelsToUnit(i)

  val font = Copperplate
  val color = Color.black
  val lineHeight = 29 pixels

  lazy val message = new TextLabel("", AlignLeft, Arial, Color.black, (4f, 0.4f), 200, app.getAssetManager())

  class FightLabel(f: MarshalledViewerFightSummary) extends Node("FightLabel") {
    val bg = createTexturedPanel("FightLabelBack", "Poule/Overview/bar_" + (if (f.order.toInt.odd_?) "light" else "dark") + ".png", SpectatorScreen.upp, true)
    val spacing = 0.04f
    val order = new TextLabel(f.order.toString, AlignRight, font, color, (30 pixels, 22 pixels), 400, app.getAssetManager(), RenderState.FaceCullMode.Back)
    val a = new TextLabel(f.fighterA.shortName, AlignLeft, font, Color.red, (750 pixels, lineHeight), 400, app.getAssetManager(), RenderState.FaceCullMode.Back)
    val b = new TextLabel(f.fighterB.shortName, AlignRight, font, Color.blue, (750 pixels, lineHeight), 400, app.getAssetManager(), RenderState.FaceCullMode.Back)
    val resultsText = if (f.finished) {
      "%d (%d-%d-%d) %d" format (f.score.a, f.score.aAfter, f.score.double, f.score.bAfter, f.score.b)
    } else if (f.started) {
      "in progress"
    } else {
      ""
    }
    val results = new TextLabel(resultsText, AlignCenter, font, color, (204 pixels, lineHeight), 400, app.getAssetManager(), RenderState.FaceCullMode.Back)
    order.setLocalTranslation(6 pixels, 14 pixels, 0.01f)
    a.setLocalTranslation(46 pixels, 12 pixels, 0.01f)
    b.setLocalTranslation(46 pixels, 12 pixels, 0.01f)
    results.setLocalTranslation(340 pixels, 12 pixels, 0.01f)
    attachChild(bg)
    attachChild(order)
    attachChild(a)
    attachChild(b)
    attachChild(results)
  }

  var fightLabels = List[FightLabel]()

  lazy val tournamentName = new TextLabel("Tournament X", AlignLeft, font, color, (950 px, 30 px), 400, app.getAssetManager(), RenderState.FaceCullMode.Back)
  lazy val roundName = new TextLabel("Round x", AlignRight, font, color, (950 px, 30 px), 400, app.getAssetManager(), RenderState.FaceCullMode.Back)
  lazy val pouleName = new TextLabel("Poule x", AlignCenter, font, color, (950 px, 40 px), 400, app.getAssetManager(), RenderState.FaceCullMode.Back)

  def initializeScene() {
    val bg = createTexturedPanel("Background", "Poule/background.jpg", SpectatorScreen.upp, false)
    val headerFooter = createTexturedPanel("HeaderFooter", "Poule/bars.png", SpectatorScreen.upp, true)
    for ((p, i) <- List(headerFooter, bg).zipWithIndex) {
      p.setLocalTranslation(-SpectatorScreen.ratio, -1f, -1f - i)
      rootNode.attachChild(p)
    }

    for (n <- List(tournamentName, roundName, pouleName)) {
      n.setLocalTranslation(-475 px, 1 - (50 px), 1f)
      rootNode.attachChild(n)
    }
  }

  def updateTextLabels(p: MarshalledViewerPool) {
    tournamentName.text = p.summary.round.tournament.name
    roundName.text = p.summary.round.name
    pouleName.text = "Poule " + p.summary.order

    for (l <- fightLabels) l.removeFromParent()

    fightLabels = for (f <- p.fights) yield new FightLabel(f)
    val baseY = 1 - (115 pixels)

    for ((l, i) <- fightLabels.zipWithIndex) {
      l.setLocalTranslation(-400 pixels, baseY - i * 0.105f, 0.1f)
      rootNode.attachChild(l)
    }

  }

}

