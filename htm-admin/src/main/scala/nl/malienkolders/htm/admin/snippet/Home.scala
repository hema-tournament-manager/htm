package nl.malienkolders.htm.admin.snippet

import net.liftweb._
import http._
import common._
import util.Helpers._
import nl.malienkolders.htm.admin.lib.exporter.ResultsExporter

object Home {
  def render = {
    "button" #> SHtml.button("Download results", () => throw new ResponseShortcutException(downloadSchedule), "class" -> "btn btn-default btn-lg")
  }

  def downloadSchedule() = {
    OutputStreamResponse(ResultsExporter.doExport _, List("content-disposition" -> ("inline; filename=\"results.xls\"")))
  }
}