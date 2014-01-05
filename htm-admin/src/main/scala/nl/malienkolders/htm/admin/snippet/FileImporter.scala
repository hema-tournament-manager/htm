package nl.malienkolders.htm.admin.snippet

import net.liftweb._
import http._
import common._
import util.Helpers._
import scala.xml.NodeSeq
import nl.malienkolders.htm.lib.model._
import nl.htm.importer.{ Importer => ImporterImpl, InputStreamSettings }
import nl.htm.importer.swordfish._
import java.io.ByteArrayInputStream

object FileImporter {

  def render = {
    var upload: Box[FileParamHolder] = Empty

    def process() = {
      PoolFight.bulkDelete_!!()
      EliminationFight.bulkDelete_!!()
      TournamentParticipants.bulkDelete_!!()
      Participant.bulkDelete_!!()
      PoolPhase.bulkDelete_!!()
      EliminationPhase.bulkDelete_!!()
      Tournament.bulkDelete_!!()
      val data = upload match {
        case Full(FileParamHolder(_, mime, fileName, file)) =>
          Some(Swordfish2013ExcelImporter.doImport(SwordfishExcelSettings(new ByteArrayInputStream(file), Country.findAll.map(c => c.code2.get -> c.name.get))))
        case _ =>
          S.notice("You have to choose a file")
          None
      }
      data match {
        case Some(eventData) =>
          //TODO: fix importer! ParticipantImporter.doImport(eventData)
          S.notice("Import succeeded")
          S.redirectTo("/tournaments/list")
        case _ =>
          S.notice("Import failed")
          S.redirectTo("/import")
      }
    }

    "#file" #> SHtml.fileUpload(f => upload = Full(f), "class" -> "form-input") &
      "#doImportFile" #> SHtml.onSubmitUnit(process)
  }

}