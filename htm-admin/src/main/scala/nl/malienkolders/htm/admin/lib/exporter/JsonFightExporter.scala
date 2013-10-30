package nl.malienkolders.htm.admin.lib.exporter

import nl.malienkolders.htm.admin.lib.FightExporter
import nl.malienkolders.htm.lib.model._
import java.io.PrintWriter
import java.util.Date
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

/**
 * - All values must be quoted
 * - Time must be rendered as Unix timestamps (java.util.Date.getTime() / 1000)
 */
object JsonFightExporter extends FightExporter {

  implicit def renderParticipant(p: Participant): JValue =
    ("ID" -> p.externalId.get) ~
      ("Name" -> p.shortName.get) ~
      ("Club" -> p.clubCode.get)

  implicit def renderTournament(t: Tournament): JValue =
    ("Type" -> t.name.get) ~
      ("ID" -> t.id.get.toString) ~
      ("StartTime" -> "0") ~
      ("Delay" -> "0") ~
      ("Round" -> t.rounds.toList)

  implicit def renderRound(r: Round): JValue =
    ("Name" -> r.name.get) ~
      ("Arena" -> List(
        ("Name" -> "Arena 1") ~
          ("ID" -> "1") ~
          ("Fight" -> r.pools.flatMap(_.fights.toList))))

  implicit def renderFight(f: Fight): JValue =
    ("Fighter_1" -> f.fighterA.foreign.get.externalId.get) ~
      ("Fighter_2" -> f.fighterB.foreign.get.externalId.get) ~
      ("Score_1" -> f.currentScore.red.toString) ~
      ("Score_2" -> f.currentScore.blue.toString) ~
      ("Doubles" -> f.currentScore.double.toString) ~
      ("Status" -> fightStatus(f))

  def fightStatus(f: Fight): String = if (f.finished_?) "finished" else if (f.started_?) "fighting" else "pending"

  def createExport: JValue =
    "Event" -> (
      ("Title" -> "Swordfish 2013") ~
      ("Updated" -> (System.currentTimeMillis() / 1000L).toString) ~
      ("Contestants" -> Participant.findAll.filter(_.subscriptions.size > 0)) ~
      ("Tournament" -> Tournament.findAll))

  def doExport: Unit = {
    val output = createExport

    val out = new PrintWriter("export.json")
    out.println(pretty(render(output)))
    out.close()
  }

}
