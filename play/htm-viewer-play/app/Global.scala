import play.api._
import nl.malienkolders.htm.lib.util.Helpers

object Global extends GlobalSettings {
  override def onStart(app: Application): Unit = {
    Helpers.openUrlFromSystemProperty("htm.viewer.url")
  }
}