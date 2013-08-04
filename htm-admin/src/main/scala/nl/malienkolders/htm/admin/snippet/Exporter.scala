package nl.malienkolders.htm.admin.snippet

import net.liftweb._
import http._
import util.Helpers._
import scala.xml.NodeSeq
import nl.malienkolders.htm.admin.lib.ParticipantImporter
import nl.malienkolders.htm.lib.model._
import nl.malienkolders.htm.admin.lib.FightExporter

object Exporter {

  def render = {
    def process() {
      FightExporter.doExport
      S.notice("Export succeeded")
      S.redirectTo("/tournaments/list")
    }
    "#doExport" #> SHtml.onSubmitUnit(process)
  }

}