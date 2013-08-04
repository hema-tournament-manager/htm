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
import nl.malienkolders.htm.lib.model._
import nl.malienkolders.htm.viewer.jmonkey.SpectatorScreen
import java.io.File
import com.jme3.cinematic.events.RotationTrack
import com.jme3.cinematic.Cinematic
import com.jme3.animation.LoopMode
import com.jme3.cinematic.events.CinematicEventListener
import com.jme3.cinematic.events.CinematicEvent
import com.jme3.animation.SpatialTrack
import com.jme3.app.state.AppStateManager

object FightAppState extends nl.malienkolders.htm.viewer.jmonkey.lib.state.FightAppState with MessageAppState {
  val textLabelFont = "Arial Bold"
  val numberLabelFont = "Raavi Bold"

  case class PixelsToUnit(value: Int) {
    def pixels = value * SpectatorScreen.upp
  }
  implicit def intToPixelsToUnit(i: Int): PixelsToUnit = PixelsToUnit(i)

  lazy val message = new TextLabel("", AlignLeft, "Arial Bold", Color.black, (SpectatorScreen.ratio * 2, 36 pixels), SpectatorScreen.ppu.toInt, app.getAssetManager())
  val messagePositionShown = (-SpectatorScreen.ratio + 0.05f, -1f + (10 pixels), 10f)
  val messagePositionHidden = (-SpectatorScreen.ratio + 0.05f, -1.1f, 10f)

  lazy val tournamentBanners = Map(List("longsword_open", "longsword_ladies", "rapier_dagger", "sword_buckler", "wrestling", "sabre").map(n =>
    (n -> createTexturedPanel("banner_" + n, "Fight/Header/banner_" + n + ".jpg", false))): _*)

  def createFighterBar = {
    val quad = new Quad(SpectatorScreen.ratio * 2, -120 pixels, true);
    val bar = new Geometry("FighterBar", quad);
    bar.setQueueBucket(RenderQueue.Bucket.Transparent)
    bar.setMaterial(createFighterBarMaterial("", ""))
    bar
  }

  def createFighterBarMaterial(countryA: String, countryB: String) = {
    val texture = FighterPanelTextureUtil.getTexture(countryA, countryB, app.getAssetManager())
    val material = new Material(app.getAssetManager(),
      "Common/MatDefs/Misc/Unshaded.j3md")
    material.setTexture("ColorMap", texture)
    material.setTransparent(true)
    material.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Back)
    material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha)
    material
  }

  def setFighterBarTexture(fighterA: MarshalledParticipant, fighterB: MarshalledParticipant) {
    fighterBar.setMaterial(createFighterBarMaterial(fighterA.country, fighterB.country))
  }

  def createAvatar(side: AvatarTextureUtil.Generated) = {
    val quad = new Quad(AvatarTextureUtil.WIDTH pixels, AvatarTextureUtil.HEIGHT pixels)
    val avatar = new Geometry("Avatar" + side.toString(), quad)
    avatar.setQueueBucket(RenderQueue.Bucket.Transparent)
    avatar.setMaterial(createAvatarTextures("default", AvatarTextureUtil.PLACEHOLDER, side))
    avatar
  }

  def createAvatarTextures(tournamentName: String, fId: String, side: AvatarTextureUtil.Generated) = {
    val texture = AvatarTextureUtil.getTexture(fId, side, tournamentName, app.getAssetManager())
    val material = new Material(app.getAssetManager(),
      "Common/MatDefs/Misc/Unshaded.j3md")
    material.setTexture("ColorMap", texture)
    material.setTransparent(true)
    material.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Back)
    material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha)
    material
  }

  def setAvatarTextures(t: String, a: MarshalledParticipant, b: MarshalledParticipant) {
    avatarRed.setMaterial(createAvatarTextures(t, a.externalId, AvatarTextureUtil.Left))
    avatarBlue.setMaterial(createAvatarTextures(t, b.externalId, AvatarTextureUtil.Right))
  }

  def createRoller = {
    val w = 203
    val h = 166
    def uw = w pixels
    def uh = h pixels
    def huw = uw / 2
    def huh = uh / 2

    def createBanner(idx: Int) = {
      val texture = app.getAssetManager().loadTexture("Fight/Header/tumbler" + idx + ".jpg")

      val quad = new Quad(uw, -uh, true);
      val grid = new Geometry("Banner", quad);
      val material = new Material(app.getAssetManager(),
        "Common/MatDefs/Misc/Unshaded.j3md");
      material.setTexture("ColorMap", texture);
      material.setTransparent(false);
      material.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Back);
      grid.setMaterial(material);
      grid
    }
    val pivot = new Node("Roller")
    val box = new Box(Vector3f.ZERO, 1, 1, 1)
    val face1 = createBanner(1)
    face1.setLocalTranslation(0, huh, huh)
    pivot.attachChild(face1)
    val face2 = createBanner(2)
    face2.rotate(FastMath.PI / 2f, 0f, 0f)
    face2.setLocalTranslation(0, -huh, huh)
    pivot.attachChild(face2)
    val face3 = createBanner(3)
    face3.rotate(FastMath.PI, 0f, 0f)
    face3.setLocalTranslation(0, -huh, -huh)
    pivot.attachChild(face3)
    val face4 = createBanner(4)
    face4.rotate(FastMath.PI * 1.5f, 0f, 0f)
    face4.setLocalTranslation(0, huh, -huh)
    pivot.attachChild(face4)
    pivot

  }
  lazy val roller = createRoller

  lazy val fighterBar = createFighterBar
  lazy val avatarRed = createAvatar(AvatarTextureUtil.Left)
  lazy val avatarBlue = createAvatar(AvatarTextureUtil.Right)
  lazy val fighterRedName = createFighterNameLabel(AlignLeft)
  lazy val fighterRedClub = createFighterClubLabel(AlignLeft)
  lazy val fighterBlueName = createFighterNameLabel(AlignRight)
  lazy val fighterBlueClub = createFighterClubLabel(AlignRight)

  def createScoreLabel = createNumericLabel("0")
  def createFighterNameLabel(align: Align) = new TextLabel("Een Hele Lange Naam", align, "Copperplate Gothic Bold", Color.white, (SpectatorScreen.ratio - 0.15f, 0.1f), 300, app.getAssetManager())
  def createFighterClubLabel(align: Align) = new TextLabel("Een Hele Lange Vereniging", align, "Copperplate Gothic Bold", Color.white, (SpectatorScreen.ratio - 0.15f, 0.06f), 300, app.getAssetManager())

  def createNumericLabel(initialValue: String, alignment: BitmapFont.Align = BitmapFont.Align.Center) = {
    val label = new BitmapText(app.getAssetManager().loadFont("Interface/Fonts/Raavi.fnt"), false)
    label.setColor(ColorRGBA.Black)
    label.setText(initialValue)
    label.setQueueBucket(RenderQueue.Bucket.Transparent)
    label.setBox(new font.Rectangle(0f, 0f, 250f, 100f))
    label.setAlignment(alignment)
    label
  }

  lazy val timerLabel = createNumericLabel("00:00")

  def createTexturedPanel(geometryName: String, textureName: String, transparent: Boolean) = {
    val texture = app.getAssetManager().loadTexture(textureName)
    val quad = new Quad(texture.getImage().getWidth() pixels, texture.getImage().getHeight() pixels)
    val panel = new Geometry(geometryName, quad)
    val material = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md")
    material.setTexture("ColorMap", texture)
    material.setTransparent(transparent)
    if (transparent) {
      material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha)
      panel.setQueueBucket(RenderQueue.Bucket.Transparent)
    }
    panel.setMaterial(material)
    panel
  }

  def hideTournamentBanners {
    tournamentBanners.values.foreach(_.setLocalTranslation(-1000f, -1000f, -1000f))
  }

  override def initializeScene() {
    super.initializeScene()

    hideTournamentBanners
    tournamentBanners.values.foreach(rootNode.attachChild _)

    scoreLabels(0).setLocalScale(0.00272f)
    scoreLabels(0).setLocalTranslation(-1.4255f, 0.369f, 0.1f)
    scoreLabels(1).setLocalScale(0.0012f)
    scoreLabels(1).setLocalTranslation(-1.14f, 0.167f, 0.1f)
    scoreLabels(2).setLocalScale(0.00272f)
    scoreLabels(2).setLocalTranslation(0.743f, 0.369f, 0.1f)
    scoreLabels(3).setLocalScale(0.0012f)
    scoreLabels(3).setLocalTranslation(1.019f, 0.167f, 0.1f)
    scoreLabels(4).setLocalScale(0.00175f)
    scoreLabels(4).setLocalTranslation(-0.225f, 0.288f, 0.1f)
    scoreLabels(5).setLocalScale(0.001346f)
    scoreLabels(5).setLocalTranslation(-0.401f, 0.882f, 0.1f)

    timerLabel.setLocalScale(0.001518f)
    timerLabel.setLocalTranslation(-0.2487f, 0.666f, 0.1f)

    for { l <- scoreLabels } rootNode.attachChild(l)
    rootNode.attachChild(timerLabel)

    phaseName.setLocalScale(0.23f)
    phaseName.setLocalTranslation(-1.308f, 0.9f, 0.1f)
    rootNode.attachChild(phaseName)

    roundName.setLocalScale(0.4f, 0.35f, 1f)
    roundName.setLocalTranslation(-1.308f, 0.68f, 0.1f)
    rootNode.attachChild(roundName)

    fightName.setLocalScale(0.4f, 0.35f, 1f)
    fightName.setLocalTranslation(-1.308f, 0.57f, 0.1f)
    rootNode.attachChild(fightName)

    exchangeLimit.setLocalScale(0.001346f)
    exchangeLimit.setLocalTranslation(-0.08f, 0.882f, 0.1f)
    rootNode.attachChild(exchangeLimit)

    fighterBar.setLocalTranslation(-SpectatorScreen.ratio, -0.482f, 0.05f)
    rootNode.attachChild(fighterBar)

    fighterRedName.setLocalTranslation(-1.136f, -0.646f, 0.1f)
    rootNode.attachChild(fighterRedName)
    fighterRedClub.setLocalTranslation(-1.128f, -0.717f, 0.1f)
    rootNode.attachChild(fighterRedClub)
    fighterBlueName.setLocalTranslation(-0.159f + 0.15f, -0.646f, 0.1f)
    rootNode.attachChild(fighterBlueName)
    fighterBlueClub.setLocalTranslation(-0.155f + 0.15f, -0.717f, 0.1f)
    rootNode.attachChild(fighterBlueClub)

    //    tournamentBanner.setLocalTranslation(0.383f, 0.934f, 2f)
    //    rootNode.attachChild(tournamentBanner)

    val background = List(
      createTexturedPanel("Points", "Fight/points.png", true),
      createTexturedPanel("HeaderFooter", "Fight/header_footer.png", true),
      createTexturedPanel("Backdrops", "Fight/backdrops.png", true),
      createTexturedPanel("Background", "Fight/background.png", false))

    for ((bg, i) <- background.zipWithIndex.reverse) {
      bg.removeFromParent()
      bg.setLocalTranslation(-SpectatorScreen.ratio, -1f, -i.toFloat)
      rootNode.attachChild(bg)
    }

    avatarRed.setLocalTranslation((67 - 512) pixels, (384 - 684) pixels, -1.5f)
    avatarBlue.setLocalTranslation((643 - 512) pixels, (384 - 684) pixels, -1.5f)

    rootNode.attachChild(avatarRed)
    rootNode.attachChild(avatarBlue)

    roller.setLocalTranslation((645 - 512) * SpectatorScreen.upp, (384 - 83) * SpectatorScreen.upp, 1f)
    rootNode.attachChild(roller)
  }

  lazy val rollerCinematics = {
    val rot = List(
      new RotationTrack(roller, new Quaternion(Array(FastMath.PI / 2f, 0f, 0f)), 1, LoopMode.DontLoop),
      new RotationTrack(roller, new Quaternion(Array(FastMath.PI, 0f, 0f)), 1, LoopMode.DontLoop),
      new RotationTrack(roller, new Quaternion(Array(FastMath.PI * 1.5f, 0f, 0f)), 1, LoopMode.DontLoop),
      new RotationTrack(roller, new Quaternion(Array(0f, 0f, 0f)), 1, LoopMode.DontLoop))

    val rollerRoll1 = new Cinematic(rootNode, 6f, LoopMode.DontLoop)
    rollerRoll1.addCinematicEvent(1, rot(0))
    val rollerRoll2 = new Cinematic(rootNode, 6f, LoopMode.DontLoop)
    rollerRoll2.addCinematicEvent(1, rot(1))
    val rollerRoll3 = new Cinematic(rootNode, 6f, LoopMode.DontLoop)
    rollerRoll3.addCinematicEvent(1, rot(2))
    val rollerRoll4 = new Cinematic(rootNode, 6f, LoopMode.DontLoop)
    rollerRoll4.addCinematicEvent(1, rot(3))

    rollerRoll1.addListener(new CinematicEventListener {
      def onPlay(e: CinematicEvent) {}
      def onPause(e: CinematicEvent) {}
      def onStop(e: CinematicEvent) {
        rollerRoll2.setTime(0)
        rollerRoll2.play()
      }
    })
    rollerRoll2.addListener(new CinematicEventListener {
      def onPlay(e: CinematicEvent) {}
      def onPause(e: CinematicEvent) {}
      def onStop(e: CinematicEvent) {
        rollerRoll3.setTime(0)
        rollerRoll3.play()
      }
    })
    rollerRoll3.addListener(new CinematicEventListener {
      def onPlay(e: CinematicEvent) {}
      def onPause(e: CinematicEvent) {}
      def onStop(e: CinematicEvent) {
        rollerRoll4.setTime(0)
        rollerRoll4.play()
      }
    })
    rollerRoll4.addListener(new CinematicEventListener {
      def onPlay(e: CinematicEvent) {}
      def onPause(e: CinematicEvent) {}
      def onStop(e: CinematicEvent) {
        rollerRoll1.setTime(0)
        rollerRoll1.play()
      }
    })
    List(rollerRoll1, rollerRoll2, rollerRoll3, rollerRoll4)
  }

  lazy val phaseName = new TextLabel("poule phase", AlignLeft, "Copperplate Gothic Bold", Color.black, (4f, 0.5f), 200, app.getAssetManager())
  lazy val roundName = new TextLabel("ROUND 6", AlignLeft, "Raavi Bold", Color.black, (4f, 0.4f), 200, app.getAssetManager())
  lazy val fightName = new TextLabel("FIGHT 29", AlignLeft, "Raavi Bold", Color.black, (4f, 0.4f), 200, app.getAssetManager())
  lazy val exchangeLimit = createNumericLabel("10")

  def tournamentBannerResourceName(tournamentIdentifier: Option[String]) = "Fight/Header/" + tournamentIdentifier.map("banner_" + _).getOrElse("banner") + ".jpg"
  def createTournamentBannerMaterial(tournamentIdentifier: Option[String]) = {
    val texture = app.getAssetManager().loadTexture(tournamentBannerResourceName(tournamentIdentifier))
    val material = new Material(app.getAssetManager(),
      "Common/MatDefs/Misc/Unshaded.j3md")
    material.setTexture("ColorMap", texture)
    material.setTransparent(false)
    material.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Back)
    material
  }

  def updateTextLabels(f: MarshalledViewerFight) {
    hideTournamentBanners
    val banner = tournamentBanners(f.tournament.identifier)
    banner.setLocalTranslation((857 - 512) pixels, (384 - 140) pixels, 10f)
    f.roundName.split("/") match {
      case Array(phase, round) =>
        phaseName.text = phase.toLowerCase().trim()
        roundName.text = round.toUpperCase().trim()
      case Array(round) =>
        phaseName.text = ""
        roundName.text = round.toUpperCase()
      case _ =>
        phaseName.text = ""
        roundName.text = ""
    }
    fightName.text = "FIGHT %d" format f.order
    exchangeLimit.setText(f.exchangeLimit.toString())

    setFighterBarTexture(f.fighterA, f.fighterB)
    setAvatarTextures(f.tournament.identifier, f.fighterA, f.fighterB)
    fighterRedName.text = f.fighterA.shortName
    fighterRedClub.text = if (f.fighterA.club.length() > 32) f.fighterA.clubCode else f.fighterA.club
    fighterBlueName.text = f.fighterB.shortName
    fighterBlueClub.text = if (f.fighterB.club.length() > 32) f.fighterB.clubCode else f.fighterB.club
  }

  override def stateAttached(stateManager: AppStateManager) {
    super.stateAttached(stateManager)
    for (c <- rollerCinematics) {
      c.stop
      c.setTime(0)
      stateManager.attach(c)
    }
    rollerCinematics(0).play
  }

  override def stateDetached(stateManager: AppStateManager) {
    for (c <- rollerCinematics) {
      c.stop
      stateManager.detach(c)
    }
    super.stateDetached(stateManager)
  }

}

