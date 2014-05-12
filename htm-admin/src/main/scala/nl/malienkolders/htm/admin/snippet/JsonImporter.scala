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

object JsonImporter extends Loggable {

  def render = {
    var url = "http://www.ghfs.se/swordfish-attendee.php"
    var json = ""

    def processJson() = {

      implicit class JoinHelper[A](list: Seq[A]) {
        def outerJoin[B](other: Seq[B]) = list.map(Some(_)).zipAll(other.map(Some(_)), None, None)
      }

      val event = EventParser.parse(json)
      Event.theOne.name(event.name).save()

      val arenas = Arena.findAll()
      // add arenas until there are enough
      for (i <- (arenas.size + 1) to event.arenas) { Arena.create.name("Arena " + i).save() }
      // drop arenas that are too many
      arenas.drop(event.arenas).foreach(_.delete_!)

      val days = Day.findAll().outerJoin(event.days)
      days.foreach {
        case (Some(day), Some(dayDef)) => day.date(dayDef.date.yyyymmdd).save()
        case (None, Some(dayDef)) => Day.create.date(dayDef.date.yyyymmdd).save()
        case (Some(day), None) => day.delete_!
        case (None, None) => // how did this happen?.map(Some(_)), None, None
      }

      Day.findAll().zipWithIndex.foreach {
        case (d, i) =>
          Arena.findAll().foreach { a =>
            val arenaTimeSlots = a.timeslots.filter(_.day.is == d.id.is)
            val arenaTimeSlotsZipped = arenaTimeSlots.outerJoin(event.days(i).timeslots)
            println(arenaTimeSlotsZipped)
            arenaTimeSlotsZipped.foreach {
              case (Some(timeslot), Some(timeslotDef)) => timeslot.name(timeslotDef.name).from(timeslotDef.from.hhmm).to(timeslotDef.to.hhmm).fightingTime(true).save()
              case (None, Some(timeslotDef)) => ArenaTimeSlot.create.arena(a).day(d).name(timeslotDef.name).from(timeslotDef.from.hhmm).to(timeslotDef.to.hhmm).fightingTime(true).save()
              case (Some(timeslot), None) => timeslot.delete_!
              case _ => // ignore
            }
          }
      }

      def copyTournament(t: Tournament, tDef: TournamentDef) = {
        t.name(tDef.name)
          .identifier(tDef.id)
          .mnemonic(tDef.mnemonic)
        t.poolPhase.ruleset(tDef.poolPhase.map(_.ruleset).getOrElse("")).save()
        t.eliminationPhase.ruleset(tDef.eliminationPhase.map(_.ruleset).getOrElse("")).save()
        t.finalsPhase.ruleset(tDef.finalsPhase.ruleset).save()
        for (generate <- tDef.generate) {
          generate match {
            case "finals" =>
              for (i <- t.finalsPhase.fights.size to 1) {
                logger.info("Finals phase: " + t.finalsPhase.name)
                t.finalsPhase.eliminationFights += EliminationFight.create
                  .round(i + 1)
                  .name(i match { case 0 => "3rd Place" case 1 => "1st Place" case _ => "" })
                  .fighterAFuture(SpecificFighter(None).format)
                  .fighterBFuture(SpecificFighter(None).format)
              }
              t.finalsPhase.save()
          }
        }
        t
      }

      Tournament.findAll().outerJoin(event.tournaments) foreach {
        case (Some(tournament), Some(tournamentDef)) => copyTournament(tournament, tournamentDef).save()
        case (None, Some(tournamentDef)) => copyTournament(Tournament.create, tournamentDef).save()
        case (Some(tournament), None) => tournament.delete_!
        case _ => // ignore
      }

      S.redirectTo("/tournaments/list")
    }

    "#json" #> SHtml.textarea(json, json = _, "class" -> "form-input") &
      "#doImportJson" #> SHtml.onSubmitUnit(() => processJson())
  }

}