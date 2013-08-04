package nl.malienkolders.htm.viewer.jmonkey.lib.state
package spectator

import nl.malienkolders.htm.viewer.jmonkey.lib.util._
import java.awt.Color
import nl.malienkolders.htm.viewer.jmonkey.lib._
import nl.malienkolders.htm.viewer.jmonkey.lib.util._
import java.awt.Font
import com.jme3.font.BitmapText
import com.jme3.math.ColorRGBA
import com.jme3.font.BitmapFont
import nl.malienkolders.htm.lib.model.MarshalledViewerFight
import com.jme3.scene.Node
import nl.malienkolders.htm.viewer.jmonkey.SpectatorScreen
import com.jme3.math.Quaternion
import com.jme3.math.FastMath

object EmptyScreenAppState extends nl.malienkolders.htm.viewer.jmonkey.lib.state.EmptyScreenAppState {

  case class PixelsToUnit(value: Int) {
    def pixels = value * SpectatorScreen.upp
    def px = pixels
  }
  implicit def intToPixelsToUnit(i: Int): PixelsToUnit = PixelsToUnit(i)

  lazy val htmLabel = {
    val pivot = new Node("HtmPivot")
    val label = new TextLabel("HTM", Color.white, (3, 1.5f), 200, app.getAssetManager())
    label.setLocalTranslation(-1.5f, -0.75f, 0f)
    pivot.attachChild(label)
    pivot
  }

  lazy val htmLogo = {
    val pivot = new Node("HtmLogoPivot")
    val logo = createTexturedPanel("Logo", "Empty/logo.png", SpectatorScreen.upp, true)
    val backLogo = createTexturedPanel("Logo", "Empty/logo.png", SpectatorScreen.upp, true)
    logo.setLocalTranslation(-219 px, -274 px, 0f)
    backLogo.setLocalTranslation(219 px, -274 px, 0f)
    backLogo.setLocalRotation(new Quaternion(Array(0f, FastMath.PI, 0f)))
    pivot.attachChild(logo)
    pivot.attachChild(backLogo)
    pivot
  }

  override def initializeScene() {
    val bg = createTexturedPanel("Background", "Empty/background.jpg", SpectatorScreen.upp, false)
    bg.setLocalTranslation(-SpectatorScreen.ratio, -1f, -10f)
    rootNode.attachChild(bg)
    rootNode.attachChild(htmLogo)
  }

  override def update(tpf: Float) {
    htmLabel.rotate(0, tpf, 0)
    htmLogo.rotate(0, tpf / 2, 0)
  }

}

