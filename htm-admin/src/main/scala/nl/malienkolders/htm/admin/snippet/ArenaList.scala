package nl.malienkolders.htm.admin.snippet

import net.liftweb._
import http._
import mapper._
import util.Helpers._
import scala.xml.NodeSeq
import nl.malienkolders.htm.lib.model._
import java.text.SimpleDateFormat
import java.util.Date
import nl.malienkolders.htm.admin.lib.exporter.ScheduleExporter
import net.liftweb.http.OutputStreamResponse
import scala.xml.Text

class ArenaList {

  val df = new SimpleDateFormat("HH:mm")

  def render = {
    val colspan = 12 / Arena.count.max(1)
    ".arena" #> Arena.findAll.map(a =>
      ".arena [class+]" #> ("col-md-" + colspan) &
        ".arenaName" #> a.name.get &
        ".download-schedule" #> SHtml.link("/download/participants", () => throw new ResponseShortcutException(downloadSchedule(a)), Text("Download schedule")) &
        ".timeslot" #> a.timeslots.map(t =>
          ".header" #> (
            ".from *" #> df.format(t.from.get) &
            ".to *" #> df.format(t.to.get) &
            ".name *" #> t.name.get) &
            ".fight" #> t.fights.map { implicit sf =>
              val f = sf.fight.foreign.get
              implicit val p = f.phase.foreign.get
              implicit val t = p.tournament.foreign.get
              ".fight [class+]" #> (if (f.finished_?) "success" else "waiting") &
                ".time *" #> df.format(new Date(sf.time.is)) &
                ".tournament *" #> tournamentName &
                ".name *" #> f.name.get
            })) &
      ".unscheduled" #> (".tournament" #> Tournament.findAll().map(t =>
        ".tournamentName *" #> t.name.get &
          ".phase" #> t.phases.map(p =>
            ".phaseHeader" #> (".name *" #> p.name.get) &
              ".fight" #> p.fights.filter(_.scheduled.foreign.isEmpty).map(f =>
                ".name *" #> f.name.get))))
  }

  def phaseName(implicit t: Tournament, p: Phase[_]) =
    <a href={ "/tournaments/view/" + t.identifier.get + "#phase" + p.id.get }>
      { p.name.get }
    </a>

  def tournamentName(implicit t: Tournament) =
    <a href={ "/tournaments/view/" + t.identifier.get }>
      { t.name.get }
    </a>

  def downloadSchedule(a: Arena, onlyUnfinishedPools: Boolean = true) = {
    OutputStreamResponse(ScheduleExporter.doExport(a, onlyUnfinishedPools) _, List("content-disposition" -> ("inline; filename=\"schedule_arena_" + a.id.get + ".xls\"")))
  }

}
