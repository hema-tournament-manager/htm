package nl.malienkolders.htm.admin.snippet

import net.liftweb._
import http._
import util.Helpers._
import scala.xml.NodeSeq
import nl.malienkolders.htm.admin.lib.ParticipantImporter
import nl.malienkolders.htm.lib.model._

object Importer {

  def render = {
    var clear = false

    def process() {
      if (clear) {
        Fight.bulkDelete_!!()
        TournamentParticipants.bulkDelete_!!()
        Participant.bulkDelete_!!()
      }
      ParticipantImporter.doImport
      S.notice("Import succeeded")
      S.redirectTo("/tournaments/list")
    }
    "#clear" #> SHtml.checkbox(clear, clear = _, "id" -> "clear") &
      "#doImport" #> SHtml.onSubmitUnit(process)
  }

}