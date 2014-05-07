package nl.malienkolders.htm.admin.snippet

import net.liftweb._
import http._
import common._
import mapper._
import util.Helpers._
import scala.xml.NodeSeq
import nl.malienkolders.htm.lib.model._
import nl.htm.importer.{ Importer => ImporterImpl, EventData, Participant => ImportedParticipant, InputStreamSettings }
import nl.htm.importer.swordfish._
import net.liftweb.json._
import nl.malienkolders.htm.admin.lib.importer.{ Event => EventParser, Tournament => TournamentDef }
import nl.malienkolders.htm.admin.lib.Utils.DateTimeParserHelper

object UrlImporter extends Loggable {

  def importParticipants(ps: List[ImportedParticipant]): Unit = ps.foreach { p =>
    if (Participant.find(By(Participant.externalId, p.sourceIds.head.id)).isEmpty) {
      Participant.create.externalId(p.sourceIds.head.id)
        .name(p.name)
        .shortName(p.shortName)
        .club(p.club)
        .clubCode(p.clubCode)
        .country(Country.find(By(Country.code2, p.country)) or Country.find(By(Country.name, p.country)))
        .save()
    }
  }

  def importSubscriptions(d: EventData): Unit = for {
    (tDef, ss) <- d.subscriptions
    t <- Tournament.find(By(Tournament.identifier, tDef.id))
    (sDef, pDef) <- ss
    p <- Participant.find(By(Participant.externalId, pDef.sourceIds.head.id))
  } {
    if (TournamentParticipant.find(By(TournamentParticipant.tournament, t), By(TournamentParticipant.participant, p)).isEmpty) {
      t.subscriptions += TournamentParticipant.create
        .participant(p)
        .primary(sDef.primary)
        .fighterNumber(sDef.number)
        .experience(sDef.xp)
        .gearChecked(false)
      t.save()
    }
  }

  def render = {
    var url = "http://www.ghfs.se/swordfish-attendee.php"
    var json = ""

    def process(): () => Any = {
      val data = Swordfish2013Importer.doImport(SwordfishSettings(url, Country.findAll.map(c => c.code2.get -> c.name.get)))

      importParticipants(data.participants)

      S.notice("Import succeeded")
      S.redirectTo("/participants/list")
    }

    "#url" #> SHtml.url(url, url = _: String, "class" -> "form-input") &
      "#doImportUrl" #> SHtml.onSubmitUnit(() => process())
  }

}