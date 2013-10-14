package nl.malienkolders.htm.viewer.jmonkey.lib
package state.spectator

import nl.malienkolders.htm.lib.model._
import java.awt._
import util._
import com.jme3.scene._
import shape._
import com.jme3.material._
import com.jme3.texture._
import com.jme3.renderer.queue.RenderQueue
import javax.imageio._
import nl.malienkolders.htm.viewer.jmonkey.SpectatorScreen
import scala.collection.immutable.List
import nl.malienkolders.htm.lib.HtmHelpers._
import nl.malienkolders.htm.lib.swiss.ParticipantScores
import com.jme3.material.RenderState.FaceCullMode
import com.jme3.math.Quaternion
import com.jme3.math.FastMath
import com.jme3.cinematic.events.RotationTrack
import com.jme3.animation.LoopMode
import com.jme3.cinematic.Cinematic
import com.jme3.app.state.AppStateManager
import com.jme3.cinematic.events.CinematicEventListener
import com.jme3.cinematic.events.CinematicEvent

object PoolRankingAppState extends state.PoolRankingAppState {

  val font = Copperplate
  val color = Color.black
  val lineHeight = 29 pixels

  case class PixelsToUnit(value: Int) {
    def pixels = value * SpectatorScreen.upp
    def px = pixels
  }
  implicit def intToPixelsToUnit(i: Int): PixelsToUnit = PixelsToUnit(i)

  lazy val fighterLabelPivot = new Node("FighterLabels")

  class FighterLabel(rank: Int, f: MarshalledParticipant, s: ParticipantScores) extends Node("FighterLabel" + f.id) {
    val bg = createTexturedPanel("FightLabelBack", "Poule/Ranking/bar_" + (if (rank.odd_?) "light" else "dark") + ".png", SpectatorScreen.upp, true)
    val order = new TextLabel(rank.toString, AlignRight, font, color, (30 pixels, 22 pixels), 400, app.getAssetManager(), FaceCullMode.Back)
    val fighterName = new TextLabel(f.shortName, AlignLeft, font, Color.black, (525 pixels, lineHeight), 400, app.getAssetManager(), FaceCullMode.Back)
    val club = new TextLabel(f.clubCode, AlignRight, font, Color.black, (525 pixels, lineHeight), 400, app.getAssetManager(), FaceCullMode.Back)
    val results = if (s.fights == 0) {
      val resultNode = new Node("Results")
      val stars = List(f.q1, f.q2, f.q3).sorted.reverse.map { q => createTexturedPanel("Star", "Poule/Ranking/star" + (if (q) "" else "_gray") + ".png", SpectatorScreen.upp, true)
      }
      stars.zipWithIndex.foreach {
        case (star, i) =>
          star.setLocalTranslation((78 + (i * 16)) pixels, 8 pixels, 0.01f)
          resultNode.attachChild(star)
      }
      resultNode
    } else {
      new TextLabel((if (s.points == 0.5) "½ point" else if ((s.points * 10).toInt % 10 == 0) { if (s.points.toInt == 1) "1 point" else s.points.toInt.toString + " points" } else s.points.toInt.toString + "½ points"), AlignCenter, font, color, (204 px, lineHeight), 400, app.getAssetManager(), FaceCullMode.Back)
    }
    order.setLocalTranslation(6 px, 14 px, 0.01f)
    fighterName.setLocalTranslation(46 px, 12 px, 0.01f)
    club.setLocalTranslation(46 px, 12 px, 0.01f)
    results.setLocalTranslation(600 px, 12 px, 0.01f)
    attachChild(bg)
    attachChild(order)
    attachChild(fighterName)
    attachChild(club)
    attachChild(results)

  }

  def createBackdrop = {
    val bgTexture = app.getAssetManager().loadTexture("Poule/Overview/background.png")

    val quad = new Quad(SpectatorScreen.ratio * 2, -2f, true);
    val grid = new Geometry("Backdrop", quad);
    val material = new Material(app.getAssetManager(),
      "Common/MatDefs/Misc/Unshaded.j3md");
    material.setTexture("ColorMap", bgTexture);
    material.setTransparent(false);
    material.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Back);
    grid.setMaterial(material);
    grid.setLocalTranslation(-SpectatorScreen.ratio, 1f, -10f)
    grid
  }

  var fighterLabels = List[FighterLabel]()

  lazy val tournamentName = new TextLabel("Tournament X", AlignLeft, font, color, (950 px, 30 px), 400, app.getAssetManager(), RenderState.FaceCullMode.Back)
  lazy val roundName = new TextLabel("Round x", AlignRight, font, color, (950 px, 30 px), 400, app.getAssetManager(), RenderState.FaceCullMode.Back)
  lazy val pouleName = new TextLabel("Poule x", AlignCenter, font, color, (950 px, 40 px), 400, app.getAssetManager(), RenderState.FaceCullMode.Back)

  lazy val rollerCinematics = {
    val rot = List(
      new RotationTrack(fighterLabelPivot, new Quaternion(Array(0f, 0f, 0f)), 3, LoopMode.DontLoop),
      new RotationTrack(fighterLabelPivot, new Quaternion(Array(0f, FastMath.PI, 0f)), 3, LoopMode.DontLoop))

    val rollerRoll1 = new Cinematic(rootNode, 10f, LoopMode.DontLoop)
    rollerRoll1.addCinematicEvent(1, rot(0))

    val rollerRoll2 = new Cinematic(rootNode, 10f, LoopMode.DontLoop)
    rollerRoll2.addCinematicEvent(1, rot(1))

    List(rollerRoll1, rollerRoll2)
  }

  lazy val rollerListeners = List(new CinematicEventListener {
    def onPlay(e: CinematicEvent) {}
    def onPause(e: CinematicEvent) {}
    def onStop(e: CinematicEvent) {
      rollerCinematics(1).setTime(0)
      rollerCinematics(1).play()
    }
  },
    new CinematicEventListener {
      def onPlay(e: CinematicEvent) {}
      def onPause(e: CinematicEvent) {}
      def onStop(e: CinematicEvent) {
        rollerCinematics(0).setTime(0)
        rollerCinematics(0).play()
      }
    })

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
    rootNode.attachChild(fighterLabelPivot)
  }

  def updateTextLabels(pool: MarshalledPoolRanking) {
    tournamentName.text = pool.poolInfo.round.tournament.name
    roundName.text = pool.poolInfo.round.name
    pouleName.text = "Poule " + pool.poolInfo.order

    fighterLabelPivot.setLocalRotation(new Quaternion(Array(0f, 0f, 0f)))

    for (l <- fighterLabels) l.removeFromParent()

    fighterLabels = for (((f, s), i) <- pool.ranked.zip(pool.points).zipWithIndex) yield new FighterLabel(i + 1, f, s)
    val baseY = 1 - (122 px)

    for ((l, i) <- fighterLabels.take(16).zipWithIndex) {
      l.setLocalTranslation(-400 pixels, baseY - i * 0.105f, 0f)
      fighterLabelPivot.attachChild(l)
    }

    for ((l, i) <- fighterLabels.drop(16).zipWithIndex) {
      l.setLocalTranslation(400 pixels, baseY - i * 0.105f, 0f)
      l.setLocalRotation(new Quaternion(Array(0f, FastMath.PI, 0f)))
      fighterLabelPivot.attachChild(l)
    }

    rollerCinematics.zip(rollerListeners).foreach {
      case (c, l) =>
        c.removeListener(l)
        c.stop
        c.setTime(0f)
    }
    if (fighterLabels.size > 16) {
      rollerCinematics.zip(rollerListeners).foreach {
        case (c, l) =>
          c.addListener(l)
      }
      rollerCinematics(0).play
    }
  }

  override def stateAttached(stateManager: AppStateManager) {
    super.stateAttached(stateManager)
    for (c <- rollerCinematics) {
      stateManager.attach(c)
    }
  }

  override def stateDetached(stateManager: AppStateManager) {
    for ((c, l) <- rollerCinematics.zip(rollerListeners)) {
      c.removeListener(l)
      c.stop
      c.setTime(0f)
      stateManager.detach(c)
    }
    super.stateDetached(stateManager)
  }

}