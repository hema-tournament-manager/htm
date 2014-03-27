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

object Importer extends Loggable {

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
                .name(i match { case 0 => "3rd Place" case 1 => "1st Place" case _ => ""})
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

    "#url" #> SHtml.url(url, url = _: String, "class" -> "form-input") &
      "#doImportUrl" #> SHtml.onSubmitUnit(() => process()) &
      "#json" #> SHtml.textarea(json, json = _, "class" -> "form-input") &
      "#doImportJson" #> SHtml.onSubmitUnit(() => processJson())
  }

}