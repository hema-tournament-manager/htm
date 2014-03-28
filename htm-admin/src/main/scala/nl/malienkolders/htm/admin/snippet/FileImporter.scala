package nl.malienkolders.htm.admin.snippet

import net.liftweb._
import http._
import common._
import util.Helpers._
import scala.xml.NodeSeq
import nl.malienkolders.htm.lib.model._
import nl.htm.importer.{ Importer => ImporterImpl, InputStreamSettings, DefaultImporter, DefaultSettings }
import java.io.ByteArrayInputStream
import nl.malienkolders.htm.admin.lib.exporter.DefaultImporterExporter
import scala.xml.Text

object FileImporter {

  def render = {
    var upload: Box[FileParamHolder] = Empty

    def process() = {
      val data = upload match {
        case Full(FileParamHolder(_, mime, fileName, file)) =>
          Some(DefaultImporter.doImport(DefaultSettings(new ByteArrayInputStream(file))))
        case _ =>
          S.notice("You have to choose a file")
          None
      }
      data match {
        case Some(eventData) =>
          implicit class JoinHelper[A](list: Seq[A]) {
            def outerJoin[B](other: Seq[B]) = list.map(Some(_)).zipAll(other.map(Some(_)), None, None)
          }
          Importer.importParticipants(eventData.participants)
          Importer.importSubscriptions(eventData)

          S.notice("Import succeeded")
          S.redirectTo("/tournaments/list")
        case _ =>
          S.notice("Import failed")
          S.redirectTo("/import")
      }
    }

    "#file" #> SHtml.fileUpload(f => upload = Full(f), "class" -> "form-input") &
      "#doImportFile" #> SHtml.onSubmitUnit(process) &
      "#downloadTemplate" #> SHtml.link("/download/importer-template", () => throw new ResponseShortcutException(downloadDefaultImporterTemplate), Text("Download template for file import"))
  }

  def downloadDefaultImporterTemplate() = {
    OutputStreamResponse(DefaultImporterExporter.doExport _, List("content-disposition" -> "inline; filename=\"importer-template.xls\""))
  }

}