package nl.malienkolders.htm.admin.snippet

import net.liftweb._
import http._
import common._
import util.Helpers._
import scala.xml.NodeSeq
import nl.malienkolders.htm.admin.lib.ParticipantImporter
import nl.malienkolders.htm.lib.model._
import nl.htm.importer.{ Importer => ImporterImpl }
import nl.htm.importer.swordfish._
import nl.htm.importer.heffac._

object Importer {

  def render = {
    var clear = false
    var clearTournaments = false
    var upload: Box[FileParamHolder] = Empty
    var url = "http://www.ghfs.se/swordfish-attendee.php"

    def process(importer: ImporterImpl[_]): () => Any = {
      if (clear) {
        Fight.bulkDelete_!!()
        TournamentParticipants.bulkDelete_!!()
        Participant.bulkDelete_!!()
        if (clearTournaments) {
          Tournament.bulkDelete_!!()
        }
      }
      val data = importer match {
        case Swordfish2013Importer =>
          Some(Swordfish2013Importer.doImport(SwordfishSettings(url, Country.findAll.map(c => c.code2.get -> c.name.get))))
        case HeffacImporter =>
          upload match {
            case Full(file) =>
              Some(HeffacImporter.doImport(HeffacSettings(file.fileStream)))
            case _ =>
              S.notice("You have to choose a file")
              None
          }
      }
      data match {
        case Some(eventData) =>
          ParticipantImporter.doImport(eventData)
          S.notice("Import succeeded")
          S.redirectTo("/tournaments/list")
        case _ =>
          S.notice("Import failed")
          S.redirectTo("/import")
      }
    }

    "#clear" #> SHtml.checkbox(clear, clear = _, "id" -> "clear", "class" -> "checkbox") &
      "#clearTournaments" #> SHtml.checkbox(clearTournaments, clearTournaments = _, "id" -> "clearTournaments", "class" -> "checkbox") &
      "#url" #> SHtml.url(url, (s: String) => url = s, "class" -> "form-input") &
      "#file" #> SHtml.fileUpload(f => upload = Full(f), "class" -> "form-input") &
      "#doImportUrl" #> SHtml.onSubmitUnit(() => process(Swordfish2013Importer)) &
      "#doImportFile" #> SHtml.onSubmitUnit(() => process(HeffacImporter))
  }

}