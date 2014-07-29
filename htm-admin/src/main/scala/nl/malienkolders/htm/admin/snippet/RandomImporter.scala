package nl.malienkolders.htm.admin.snippet

import net.liftweb._
import http._
import common._
import mapper._
import util.Helpers._
import scala.xml.NodeSeq
import nl.malienkolders.htm.lib.model._
import nl.malienkolders.htm.admin.lib.importer.{ Event => EventParser, Tournament => TournamentDef }
import nl.malienkolders.htm.admin.lib.Utils.DateTimeParserHelper

import nl.htm.importer.random.{ RandomImporter => Importer, RandomSettings }

object RandomImporter {
  def render = {
    var size = 100

    def process(): () => Any = {
      val data = Importer.doImport(RandomSettings(size, Country.findAll(By(Country.hasViewerFlag, true)).map(_.code2.is), Tournament.findAll.map(_.identifier.is)))

      UrlImporter.importParticipants(data.participants)
      UrlImporter.importSubscriptions(data)

      S.notice("Import succeeded")
      S.redirectTo("/participants/list")
    }

    "#size" #> SHtml.number(size, size = _: Int, 1, 500, "class" -> "form-input") &
      "#doImportRandom" #> SHtml.onSubmitUnit(() => process())
  }
}