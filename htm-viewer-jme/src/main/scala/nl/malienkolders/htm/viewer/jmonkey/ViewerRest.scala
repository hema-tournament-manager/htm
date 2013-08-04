package nl.malienkolders.htm.viewer.jmonkey

import net.liftweb.http._
import net.liftweb.http.rest._
import net.liftweb.json._
import net.liftweb.util.Helpers._
import nl.malienkolders.htm.viewer.jmonkey.lib.Fight
import Extraction.decompose
import java.awt.GraphicsEnvironment
import nl.malienkolders.htm.viewer.jmonkey.lib.Screen
import nl.malienkolders.htm.viewer.jmonkey.lib.state.FightAppState
import nl.malienkolders.htm.lib.model.{ MarshalledViewerFight, TotalScore }
import nl.malienkolders.htm.lib._
import EncodingHelpers._
import nl.malienkolders.htm.lib.model.MarshalledViewerPool
import java.util.concurrent.Callable
import com.jme3.scene.Spatial
import nl.malienkolders.htm.lib.model.ViewerMessage
import nl.malienkolders.htm.lib.model.MarshalledPoolRanking

object ViewerRest extends RestHelper {

  override implicit val formats = Serialization.formats(ShortTypeHints(List(classOf[EmptyView], classOf[PoolOverview], classOf[PoolRanking], classOf[FightView])))

  var running = false

  serve {
    case "ping" :: Nil JsonGet _ =>
      JString("pong")
  }

  serve {
    case "poll" :: Nil JsonGet _ =>
      val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
      val screens = for (d <- ge.getScreenDevices()) yield Screen(d.getIDstring(), d.getDisplayMode().getWidth(), d.getDisplayMode().getHeight(), d.isFullScreenSupported())
      decompose(screens)

    case "state" :: Nil JsonGet _ =>
      JBool(running)

    case "boot" :: mode :: AsInt(screen) :: Nil JsonGet _ =>
      if (running) {
        println("Already running")
      } else {
        println("Booting on screen %d" format screen)
        JmeApplication.boot(mode match {
          case "spectator_screen" => SpectatorScreen
          case "live_stream" => LiveStream
        }, screen, false)
        running = true
      }
      JBool(true)

    case "shutdown" :: Nil JsonGet _ =>
      println("Shutting down")
      JmeApplication.shutdown()
      running = false
      JBool(true)

    case "message" :: Nil Post reqBase64Bytes =>
      val reqBase64String = reqBase64Bytes.body.map(b => new String(b)).getOrElse("")
      val json = decodeBase64(reqBase64String)
      val m = Serialization.read[ViewerMessage](json)
      JmeApplication.showMessage(m)
      JBool(true)

  }
  serve {
    case "score" :: Nil JsonPost json -> _ =>
      JmeApplication.fightAppState.scores = Extraction.extract[TotalScore](json)
      JBool(true)

    case "timer" :: "start" :: AsLong(currentTime) :: Nil JsonGet _ =>
      JmeApplication.fightAppState.startTimer(currentTime)
      JBool(true)

    case "timer" :: "stop" :: AsLong(currentTime) :: Nil JsonGet _ =>
      JmeApplication.fightAppState.stopTimer(currentTime)
      JBool(true)

    case "init" :: "fight" :: Nil Post reqBase64Bytes =>
      val reqBase64String = reqBase64Bytes.body.map(b => new String(b)).getOrElse("")
      val json = decodeBase64(reqBase64String)
      val f = Serialization.read[MarshalledViewerFight](json)
      JmeApplication.safeUpdate { JmeApplication.fightAppState.info(f) }
      JBool(true)
  }

  serve {
    case "switch" :: Nil JsonPost json -> _ =>
      println("SWITCH " + json)
      val v = Extraction.extract[View](json)
      JmeApplication.switch(v)
      JBool(true)

    case "change" :: AsInt(screenIdx) :: Nil JsonGet _ =>
      JmeApplication.changeScreen(screenIdx)
      JBool(true)
  }

  serve {
    case "init" :: "pool" :: Nil Post reqBase64Bytes =>
      val reqBase64String = reqBase64Bytes.body.map(b => new String(b)).getOrElse("")
      val json = decodeBase64(reqBase64String)
      val p = Serialization.read[MarshalledViewerPool](json)
      JmeApplication.safeUpdate { JmeApplication.poolAppState.info(p) }
      JBool(true)

    case "init" :: "ranking" :: Nil Post reqBase64Bytes =>
      val reqBase64String = reqBase64Bytes.body.map(b => new String(b)).getOrElse("")
      val json = decodeBase64(reqBase64String)
      val p = Serialization.read[MarshalledPoolRanking](json)
      JmeApplication.safeUpdate { JmeApplication.rankingAppState.info(p) }
      JBool(true)
  }
}