package nl.malienkolders.htm.viewer.jmonkey.lib.state
package live

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
import nl.malienkolders.htm.lib.model.MarshalledViewerFight
import nl.malienkolders.htm.viewer.jmonkey.LiveStream

object FightAppState extends nl.malienkolders.htm.viewer.jmonkey.lib.state.FightAppState {

  case class PixelsToUnit(value: Int) {
    def pixels = value * LiveStream.upp
  }
  implicit def intToPixelsToUnit(i: Int): PixelsToUnit = PixelsToUnit(i)

  lazy val message = new TextLabel("", AlignLeft, "Raavi Bold", Color.black, (4f, 0.4f), 200, app.getAssetManager())

  def createScoreLabel = createNumericLabel("0")

  def createNumericLabel(initialValue: String, alignment: BitmapFont.Align = BitmapFont.Align.Center) = {
    val label = new BitmapText(app.getAssetManager().loadFont("Interface/Fonts/Raavi.fnt"), false)
    label.setColor(ColorRGBA.White)
    label.setText(initialValue)
    label.setQueueBucket(RenderQueue.Bucket.Transparent)
    label.setBox(new font.Rectangle(0f, 0f, 250f, 100f))
    label.setAlignment(alignment)
    label
  }

  def createTimerLabel = createNumericLabel("00:00")

  lazy val timerLabel = createTimerLabel

  lazy val header = createTexturedPanel("Header", "LiveStream/Fight/header.jpg", LiveStream.upp, false)
  lazy val barLeft = createTexturedPanel("FighterBarLef", "LiveStream/Fight/bar_l.jpg", LiveStream.upp, false)
  lazy val barRight = createTexturedPanel("FighterBarRight", "LiveStream/Fight/bar_r.jpg", LiveStream.upp, false)

  def initializeScene() {
    //    scoreLabels(0).setLocalScale(0.14f)
    scoreLabels(0).setAlignment(BitmapFont.Align.Right)
    scoreLabels(0).setLocalScale(0.001737f)
    scoreLabels(0).setLocalTranslation(-0.5967f, -0.86747f, 1f)
    scoreLabels(2).setAlignment(BitmapFont.Align.Left)
    scoreLabels(2).setLocalScale(0.001737f)
    scoreLabels(2).setLocalTranslation(0.1563f, -0.86747f, 1f)

    timerLabel.setLocalScale(0.000767f)
    timerLabel.setLocalTranslation(-1.0899f, 0.8667f, 1f)

    rootNode.attachChild(scoreLabels(0))
    rootNode.attachChild(scoreLabels(2))
    rootNode.attachChild(timerLabel)

    header.setLocalTranslation((14 pixels) - LiveStream.ratio, 1f - (80 pixels), 0f)
    barLeft.setLocalTranslation((159 pixels) - LiveStream.ratio, 1f - (715 pixels), 0f)
    barRight.setLocalTranslation((684 pixels) - LiveStream.ratio, 1f - (715 pixels), 0f)

    rootNode.attachChild(header)
    rootNode.attachChild(barLeft)
    rootNode.attachChild(barRight)

    tournamentName.setLocalTranslation((115 pixels) - LiveStream.ratio, 1f - (40 pixels), 0.5f)
    roundName.setLocalTranslation((115 pixels) - LiveStream.ratio, 1f - (67 pixels), 0.5f)
    rootNode.attachChild(tournamentName)
    rootNode.attachChild(roundName)

    fighterRedName.setLocalTranslation((246 pixels) - LiveStream.ratio, 1f - (688 pixels), 0.5f)
    clubRedName.setLocalTranslation((258 pixels) - LiveStream.ratio, 1f - (705 pixels), 0.5f)
    rootNode.attachChild(fighterRedName)
    rootNode.attachChild(clubRedName)
    fighterBlueName.setLocalTranslation(LiveStream.ratio - ((246 + 280) pixels), 1f - (688 pixels), 0.5f)
    clubBlueName.setLocalTranslation(LiveStream.ratio - ((258 + 268) pixels), 1f - (705 pixels), 0.5f)
    rootNode.attachChild(fighterBlueName)
    rootNode.attachChild(clubBlueName)
  }

  lazy val tournamentName = new TextLabel("Longsword Open", AlignLeft, "Copperplate Gothic Bold", Color.white, (220 pixels, 22 pixels), 400, app.getAssetManager())
  lazy val roundName = new TextLabel("1st place", AlignLeft, "Copperplate Gothic Bold", Color.lightGray, (220 pixels, 22 pixels), 400, app.getAssetManager())
  lazy val fighterRedName = new TextLabel("Fighter Red", AlignLeft, "Copperplate Gothic Bold", Color.white, (280 pixels, 22 pixels), 400, app.getAssetManager())
  lazy val clubRedName = new TextLabel("Club Red", AlignLeft, "Copperplate Gothic Bold", Color.white, (268 pixels, 15 pixels), 600, app.getAssetManager())
  lazy val fighterBlueName = new TextLabel("Fighter Blue", AlignRight, "Copperplate Gothic Bold", Color.white, (280 pixels, 22 pixels), 400, app.getAssetManager())
  lazy val clubBlueName = new TextLabel("Club Blue", AlignRight, "Copperplate Gothic Bold", Color.white, (268 pixels, 15 pixels), 600, app.getAssetManager())

  def updateTextLabels(f: MarshalledViewerFight) {
    tournamentName.text = f.tournament.name
    roundName.text = f.roundName
    fighterRedName.text = f.fighterA.shortName
    clubRedName.text = if (f.fighterA.club.length() <= 34) f.fighterA.club else f.fighterA.clubCode
    fighterBlueName.text = f.fighterB.shortName
    clubBlueName.text = if (f.fighterB.club.length() <= 34) f.fighterB.club else f.fighterB.clubCode

    barLeft.getMaterial().setTexture("ColorMap", FighterPanelTextureUtil.getLiveTexture(true, f.fighterA.country, app.getAssetManager))
    barRight.getMaterial().setTexture("ColorMap", FighterPanelTextureUtil.getLiveTexture(false, f.fighterB.country, app.getAssetManager))
  }

}

