package nl.malienkolders.htm.viewer.jmonkey

import com.jme3.app.SimpleApplication
import com.jme3.renderer.RenderManager
import lib.state.FightAppState
import com.jme3.math.ColorRGBA
import com.jme3.system.AppSettings
import com.jme3.system.JmeCanvasContext
import java.awt.Dimension
import java.awt.EventQueue
import javax.swing.JFrame
import javax.swing.JPanel
import java.awt.BorderLayout
import java.awt.GraphicsEnvironment
import nl.malienkolders.htm.viewer.jmonkey.lib.state._
import nl.malienkolders.htm.viewer.jmonkey.lib.state.spectator.{ EmptyScreenAppState => SpectatorEmpty, PoolOverviewAppState => SpectatorPool, PoolRankingAppState => SpectatorRanking, FightAppState => SpectatorFight }
import nl.malienkolders.htm.viewer.jmonkey.lib.state.live.{ EmptyScreenAppState => LiveEmpty, PoolOverviewAppState => LivePool, PoolRankingAppState => LiveRanking, FightAppState => LiveFight }
import com.jme3.input.InputManager
import nl.malienkolders.htm.lib._
import java.util.concurrent.Callable
import nl.malienkolders.htm.lib.model._
import com.jme3.asset.plugins.FileLocator

object JmeApplication extends SimpleApplication {

  val panel = new JPanel(new BorderLayout())
  var window = new JFrame("HTM Viewer")
  window.setUndecorated(true)
  var booted = false
  var mode: RunMode = SpectatorScreen
  var testmode = false
  var emptyAppState: EmptyScreenAppState = SpectatorEmpty
  var poolAppState: PoolOverviewAppState = SpectatorPool
  var rankingAppState: PoolRankingAppState = SpectatorRanking
  var fightAppState: FightAppState = SpectatorFight

  override def simpleInitApp() {
    setDisplayFps(false);
    setDisplayStatView(false);
    setPauseOnLostFocus(false);
    flyCam.setEnabled(false);

    cam.setParallelProjection(true)
    recalculateScreenProportion
    setFrustum

    inputManager.clearMappings();
    inputManager.clearRawInputListeners();

    assetManager.registerLocator("RuntimeResources", classOf[FileLocator])

    emptyAppState.initialize(stateManager, this)
    poolAppState.initialize(stateManager, this)
    rankingAppState.initialize(stateManager, this)
    fightAppState.initialize(stateManager, this)
    currentView = new EmptyView
    stateManager.attach(emptyAppState)
    if (testmode) {
      DesignerAppState.initialize(stateManager, this)
      stateManager.attach(DesignerAppState)

      DesignerAppState.initializeData
    }

    viewPort.setBackgroundColor(mode match {
      case SpectatorScreen => ColorRGBA.Black
      case LiveStream => ColorRGBA.Green
    })
  }

  abstract class ScreenProportion
  case object WideScreen extends ScreenProportion
  case object NarrowScreen extends ScreenProportion
  var currentScreenProportion: ScreenProportion = WideScreen
  var currentScreenRatio: Float = mode.ratio

  def recalculateScreenProportion: Boolean = {
    val prevScreenRatio = currentScreenRatio
    currentScreenRatio = cam.getWidth().toFloat / cam.getHeight().toFloat
    if (currentScreenRatio <= mode.ratio)
      currentScreenProportion = NarrowScreen
    else
      currentScreenProportion = WideScreen
    currentScreenRatio != prevScreenRatio
  }

  def setFrustum {
    currentScreenProportion match {
      case WideScreen =>
        cam.setFrustum(-1000, 1000, -currentScreenRatio, currentScreenRatio, 1, -1)
      case NarrowScreen =>
        cam.setFrustum(-1000, 1000, -mode.ratio, mode.ratio, mode.ratio / currentScreenRatio, -mode.ratio / currentScreenRatio)
    }
  }

  override def simpleUpdate(tpf: Float) {
    if (recalculateScreenProportion)
      setFrustum
  }

  override def simpleRender(rm: RenderManager) {
    // TODO: add render code
  }

  def firstBoot(w: Int, h: Int) {
    val settings = new AppSettings(true)
    settings.setWidth(w)
    settings.setHeight(h)
    settings.setSamples(1)

    setSettings(settings)
    createCanvas()
    val context = getContext().asInstanceOf[JmeCanvasContext]
    context.getCanvas().setPreferredSize(new Dimension(w, h))

    EventQueue.invokeLater(new Runnable() {
      def run {
        panel.add(context.getCanvas(), BorderLayout.CENTER)
        startCanvas()
      }

    })
  }

  def boot(mode: RunMode, screen: Int, testmode: Boolean) {
    window.setVisible(false)
    window = new JFrame("HTM Viewer")

    this.mode = mode
    this.testmode = testmode

    emptyAppState = mode match {
      case SpectatorScreen => SpectatorEmpty
      case LiveStream => LiveEmpty
    }
    poolAppState = mode match {
      case SpectatorScreen => SpectatorPool
      case LiveStream => LivePool
    }
    rankingAppState = mode match {
      case SpectatorScreen => SpectatorRanking
      case LiveStream => LiveRanking
    }
    fightAppState = mode match {
      case SpectatorScreen => SpectatorFight
      case LiveStream => LiveFight
    }

    var resolution = (800, 600)
    var position = (0, 0)
    if (screen < 0) {
      window.setUndecorated(false)
      resolution = mode.resolution
    } else {
      window.setUndecorated(true)
      val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
      val sd = ge.getScreenDevices()(screen)
      val dm = sd.getDisplayMode()
      val bs = sd.getDefaultConfiguration().getBounds()
      resolution = (dm.getWidth(), dm.getHeight())
      position = (bs.getX().toInt, bs.getY().toInt)
    }

    if (!booted) {
      firstBoot(resolution._1, resolution._2)
      booted = true
    }

    window.add(panel)
    window.pack()
    window.setLocation(position._1, position._2)
    window.setVisible(true)
    window.requestFocus()
  }

  def shutdown() {
    window.setVisible(false)
  }

  def getAppStateForView(v: View) = v match {
    case _: EmptyView => emptyAppState
    case _: PoolOverview => poolAppState
    case _: PoolRanking => rankingAppState
    case _: FightView => fightAppState
  }

  var currentView: View = new FightView
  def switch(view: View) {
    safeUpdate {
      JmeApplication.getStateManager().detach(getAppStateForView(currentView))
      JmeApplication.getStateManager().attach(getAppStateForView(view))
      JmeApplication.currentView = view
    }
  }

  def changeScreen(screen: Int) {
    window.setVisible(false)
    window = new JFrame("HTM Viewer")
    var resolution = (800, 600)
    var position = (0, 0)
    if (screen < 0) {
      window.setUndecorated(false)
      resolution = mode.resolution
    } else {
      window.setUndecorated(true)
      val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
      val sd = ge.getScreenDevices()(screen)
      val dm = sd.getDisplayMode()
      val bs = sd.getDefaultConfiguration().getBounds()
      resolution = (dm.getWidth(), dm.getHeight())
      position = (bs.getX().toInt, bs.getY().toInt)
    }

    window.add(panel)

    window.setSize(resolution._1, resolution._2)
    window.setLocation(position._1, position._2)
    window.setVisible(true)
    window.requestFocus()
  }

  def safeUpdate(f: => Unit) {
    val app = this
    enqueue(new Callable[Void]() {
      override def call() = {
        f
        null
      }
    });
  }

  def showMessage(m: ViewerMessage) {
    safeUpdate {
      val view = getAppStateForView(currentView)
      if (view.isInstanceOf[MessageAppState])
        view.asInstanceOf[MessageAppState].showMessage(m)
    }
  }
}

abstract class RunMode {
  def resolution: (Int, Int)
  def ratio = resolution._1.toFloat / resolution._2.toFloat
  /**
   * units per pixel
   */
  def upp = 2f / resolution._2.toFloat
  /**
   * pixels per unit
   */
  def ppu = resolution._2.toFloat / 2f
}
case object SpectatorScreen extends RunMode { val resolution = (1024, 768) }
case object LiveStream extends RunMode { val resolution = (1280, 720) }