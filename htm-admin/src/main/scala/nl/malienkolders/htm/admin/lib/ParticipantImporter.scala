package nl.malienkolders.htm.admin.lib

import scala.xml._
import java.net.URL
import scala.io.Source
import scala.util.matching._
import nl.malienkolders.htm.lib.model._
import nl.malienkolders.htm.admin.model._
import net.liftweb.mapper._
import net.liftweb.util._
import nl.htm.importer.DummyImporter
import nl.htm.importer.swordfish.Swordfish2013Importer
import nl.htm.importer.swordfish.SwordfishSettings
import nl.htm.importer.heffac.HeffacImporter
import nl.htm.importer.EmptySettings
import nl.htm.importer.heffac.HeffacSettings
import java.io.File
import nl.htm.importer.EventData

object ParticipantImporter {

  def doImport(data: EventData) = {

    val noCountry = Country.find(By(Country.code2, "")).get

    Arena.bulkDelete_!!()
    // create as many arenas as are missing
    for (i <- 1 to data.arenas) {
      Arena.create.name("Arena " + i).save()
    }

    val tournaments = if (Tournament.count == 0) {
      data.tournaments.map { case t => Tournament.create.name(t.name).identifier(t.id).saveMe }
    } else {
      Tournament.findAll(OrderBy(Tournament.id, Ascending))
    }

    val ps = data.participants.map(p => Participant.create.externalId(p.sourceIds.head.id).
      name(p.name).
      shortName(p.shortName).
      club(p.club).
      clubCode(p.clubCode).
      country(Country.find(By(Country.code2, p.country)).getOrElse(noCountry)))

    // insert participant if it doesn't exist yet
    ps.foreach(_.save)

    // add participants to tournaments
    tournaments.foreach { t =>
      t.participants.clear
      t.save
    }
    data.subscriptions.foreach {
      case (t, tps) =>
        tournaments.find(_.identifier == t.id).foreach(t => t.participants ++= tps.map(p => ps.find(_.name == p.name).get))
    }
    tournaments.foreach(_.save)
  }

}