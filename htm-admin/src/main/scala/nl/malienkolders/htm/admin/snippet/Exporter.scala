package nl.malienkolders.htm.admin.snippet

import net.liftweb._
import http._
import util.Helpers._
import scala.xml.NodeSeq
import nl.malienkolders.htm.admin.lib.ParticipantImporter
import nl.malienkolders.htm.lib.model._
import nl.malienkolders.htm.admin.lib.FightExporter
import net.liftweb.common.Full
import nl.malienkolders.htm.admin.lib.exporter.JsonFightExporter
import nl.malienkolders.htm.admin.lib.exporter.CsvFightExporter

object Exporter {

  def render = {

    var exportType = "json"

    def process() {
      val exporter = exportType match {
        case "json" => JsonFightExporter
        case "csv" => CsvFightExporter
      }
      exporter.doExport
      S.notice("Export succeeded")
      S.redirectTo("/tournaments/list")
    }
    "#exportType" #> SHtml.select(List("json" -> "JSON", "csv" -> "CSV"), Full(exportType), exportType = _) &
      "#doExport" #> SHtml.onSubmitUnit(process)
  }

}