package nl.malienkolders.htm.admin.snippet

import net.liftweb._
import http._
import js.JsCmds._
import mapper._
import util.Helpers._
import scala.xml.NodeSeq
import nl.malienkolders.htm.lib.model._
import java.text.SimpleDateFormat
import java.util.Date
import nl.malienkolders.htm.admin.lib.exporter.ScheduleExporter
import net.liftweb.http.OutputStreamResponse
import scala.xml.Text
import java.util.TimeZone
import net.liftweb.common.Empty

class ArenaList {

  val df = new SimpleDateFormat("HH:mm")
  df.setTimeZone(TimeZone.getTimeZone("UTC"))

  def scheduleFight(f: Fight[_, _], ts: ArenaTimeSlot) = scheduleFights(Seq[Fight[_, _]](f), ts)

  def scheduleFights(fs: Seq[Fight[_, _]], ts: ArenaTimeSlot) = {
    fs.filter(_.scheduled.foreign.isEmpty).foreach { f =>
      ts.fights += f.schedule(ts.firstFreeMoment)
      ts.save()
      f.save()
    }

    Reload
  }

  def unscheduleFight(sf: ScheduledFight[_]) = unscheduleFights(Seq[ScheduledFight[_]](sf))

  def unscheduleFights(sfs: Seq[ScheduledFight[_]]) = {
    for (sf <- sfs) {
      val ts = sf.timeslot.foreign.get
      val f = sf.fight.foreign.get

      f.scheduled(Empty)
      sf.delete_!

      f.save()
      ts.save()
    }

    Reload
  }
  def render = {
    val colspan = 9 / Arena.count.max(1)
    ".arena" #> Arena.findAll.map(a =>
      ".arena [class+]" #> ("col-md-" + colspan) &
        ".arenaName" #> a.name.get &
        ".download-schedule" #> SHtml.link("/download/participants", () => throw new ResponseShortcutException(downloadSchedule(a)), Text("Download schedule")) &
        ".timeslot" #> a.timeslots.map(ts =>
          ".header" #> (
            ".name *" #> ts.name.get &
            ".from *" #> df.format(ts.from.get) &
            ".to *" #> df.format(ts.to.get) &
            ".unschedule" #> SHtml.a(() => unscheduleFights(ts.fights), Text("Unschedule"))) &
            ".fight" #> ts.fights.map { implicit sf =>
              val f = sf.fight.foreign.get
              implicit val p = f.phase.foreign.get
              implicit val t = p.tournament.foreign.get
              ".fight [class+]" #> (if (f.finished_?) "success" else "waiting") &
                ".time *" #> df.format(new Date(sf.time.is)) &
                ".tournament *" #> tournamentName &
                ".name *" #> f.name.get &
                ".unschedule" #> SHtml.a(() => unscheduleFight(sf), Text("Unschedule"))
            })) &
      ".unscheduled" #> (".tournament" #> Tournament.findAll().map(t =>
        ".tournamentName *" #> t.name.get &
          ".phase" #> t.phases.map(p =>
            ".phaseHeader" #> (
              ".name *" #> p.name.get &
              ".schedule" #> (
                ".arena" #> Arena.findAll().map(a =>
                  ".arenaName *" #> a.name.get &
                    ".day" #> Day.findAll().zipWithIndex.map {
                      case (d, i) =>
                        ".dayName *" #> ("Day " + (i + 1)) &
                          ".timeslot" #> a.timeslots.filter(_.day.is == d.id.is).filter(_.fightingTime.is).map(ts =>
                            "a" #> SHtml.a(() => scheduleFights(p.fights, ts), <span class="from">{ df.format(ts.from.get) }</span><span class="to">{ df.format(ts.to.get) }</span><span class="name">{ ts.name.get }</span>))
                    }))) &
                ".fight" #> p.fights.filter(_.scheduled.foreign.isEmpty).map(f =>
                  ".name *" #> f.name.get &
                    ".schedule" #> (
                      ".arena" #> Arena.findAll().map(a =>
                        ".arenaName *" #> a.name.get &
                          ".day" #> Day.findAll().zipWithIndex.map {
                            case (d, i) =>
                              ".dayName *" #> ("Day " + (i + 1)) &
                                ".timeslot" #> a.timeslots.filter(_.day.is == d.id.is).filter(_.fightingTime.is).map(ts =>
                                  "a" #> SHtml.a(() => scheduleFight(f, ts), <span class="name">{ ts.name.get }</span><span class="from">{ df.format(ts.from.get) }</span><span class="to">{ df.format(ts.to.get) }</span>))
                          }))))))
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
