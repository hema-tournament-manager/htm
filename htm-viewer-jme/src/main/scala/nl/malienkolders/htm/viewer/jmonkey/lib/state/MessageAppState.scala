package nl.malienkolders.htm.viewer.jmonkey.lib.state

import scala.collection.mutable.Queue
import nl.malienkolders.htm.lib.model.ViewerMessage
import nl.malienkolders.htm.viewer.jmonkey.lib.util.TextLabel
import com.jme3.app.state.AppStateManager
import com.jme3.cinematic.events.PositionTrack
import com.jme3.cinematic.Cinematic
import com.jme3.math.Vector3f
import com.jme3.animation.LoopMode
import com.jme3.cinematic.PlayState
import com.jme3.cinematic.events.CinematicEventListener
import com.jme3.cinematic.events.CinematicEvent

abstract trait MessageAppState extends TournamentAppState {
  implicit def tuple3toVector3f(t: (Float, Float, Float)) = new Vector3f(t._1, t._2, t._3)

  val messageQueue: Queue[ViewerMessage] = Queue()
  def message: TextLabel
  def messagePositionShown: (Float, Float, Float)
  def messagePositionHidden: (Float, Float, Float)
  var permanentMessage = ""
  var currentMessage = ""

  lazy val messageInTrack = new PositionTrack(message, messagePositionShown, 1f, LoopMode.DontLoop)
  lazy val messageOutTrack = new PositionTrack(message, messagePositionHidden, 1f, LoopMode.DontLoop)
  lazy val showMessage = {
    val m = new Cinematic(rootNode, 12f, LoopMode.DontLoop);
    m.addCinematicEvent(0, messageInTrack);
    m.addCinematicEvent(11, messageOutTrack);
    m.addListener(new CinematicEventListener {
      def onPlay(e: CinematicEvent) {
        message.text = currentMessage
      }
      def onPause(e: CinematicEvent) {}
      def onStop(e: CinematicEvent) {
        showPermanentMessage.play()
      }
    })
    m
  }
  lazy val showPermanentMessage = {
    val m = new Cinematic(rootNode, 1f, LoopMode.DontLoop)
    m.addCinematicEvent(0, messageInTrack)
    m.addListener(new CinematicEventListener {
      def onPlay(e: CinematicEvent) {
        message.text = permanentMessage
      }
      def onPause(e: CinematicEvent) {}
      def onStop(e: CinematicEvent) {}
    })
    m
  }
  lazy val hidePermanentMessage = {
    val m = new Cinematic(rootNode, 1f, LoopMode.DontLoop)
    m.addCinematicEvent(0, messageOutTrack)
    m.addListener(new CinematicEventListener {
      def onPlay(e: CinematicEvent) {}
      def onPause(e: CinematicEvent) {}
      def onStop(e: CinematicEvent) {
        showMessage.play()
      }
    })
    m
  }

  def initializeScene() {
    rootNode.attachChild(message)
  }

  override def stateAttached(stateManager: AppStateManager) {
    super.stateAttached(stateManager)
    app.getStateManager().attach(showMessage)
    app.getStateManager().attach(showPermanentMessage)
    app.getStateManager().attach(hidePermanentMessage)
    message.setLocalTranslation(messagePositionHidden)
    showPermanentMessage.play()
  }

  override def stateDetached(stateManager: AppStateManager) {
    app.getStateManager().detach(showMessage)
    app.getStateManager().detach(showPermanentMessage)
    app.getStateManager().detach(hidePermanentMessage)
    super.stateDetached(stateManager)
  }

  def showMessage(m: ViewerMessage) {
    messageQueue.enqueue(m)
  }

  override def update(tpf: Float) {
    super.update(tpf: Float)
    if (!messageQueue.isEmpty && showMessage.getPlayState() != PlayState.Playing && showPermanentMessage.getPlayState() != PlayState.Playing && hidePermanentMessage.getPlayState() != PlayState.Playing) {
      val m = messageQueue.dequeue
      if (m.duration > 0) {
        currentMessage = m.message
        val seconds = m.duration / 1000f
        val speed = 10f / seconds
        showMessage.setTime(0)
        showMessage.setSpeed(speed)
        hidePermanentMessage.play()
      } else {
        permanentMessage = m.message
        showPermanentMessage.play()
      }
    }
  }
}