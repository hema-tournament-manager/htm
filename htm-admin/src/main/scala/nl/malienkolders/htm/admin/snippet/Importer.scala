package nl.malienkolders.htm.admin.snippet

import net.liftweb._
import http._
import common._
import util.Helpers._
import scala.xml.NodeSeq
import nl.malienkolders.htm.lib.model._
import nl.htm.importer.{ Importer => ImporterImpl, InputStreamSettings }
import nl.htm.importer.swordfish._

object Importer {

  def render = {
    var url = "http://www.ghfs.se/swordfish-attendee.php"

    def process(): () => Any = {
      PoolFight.bulkDelete_!!()
      EliminationFight.bulkDelete_!!()
      TournamentParticipants.bulkDelete_!!()
      Participant.bulkDelete_!!()
      PoolPhase.bulkDelete_!!()
      EliminationPhase.bulkDelete_!!()
      Tournament.bulkDelete_!!()

      val data = Swordfish2013Importer.doImport(SwordfishSettings(url, Country.findAll.map(c => c.code2.get -> c.name.get)))

      //TODO: fix importer! ParticipantImporter.doImport(data)
      S.notice("Import succeeded")
      S.redirectTo("/tournaments/list")
    }

    "#url" #> SHtml.url(url, (s: String) => url = s, "class" -> "form-input") &
      "#doImportUrl" #> SHtml.onSubmitUnit(() => process())
  }

}