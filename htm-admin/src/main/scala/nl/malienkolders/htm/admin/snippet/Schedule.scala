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
import net.liftweb.http.js.JsCmd
import net.liftweb.common.Full
import net.liftweb.util.CssSel

class Schedule {

  val df = new SimpleDateFormat("HH:mm")
  df.setTimeZone(TimeZone.getTimeZone("UTC"))

  def scheduleFight(f: Fight[_, _], ts: ArenaTimeSlot) = scheduleFights(Seq[Fight[_, _]](f), ts)

  def scheduleFights(fs: Seq[Fight[_, _]], ts: ArenaTimeSlot) = {
    fs.filter(_.scheduled.foreign.isEmpty).foreach { f =>
      val phase = f.phase.foreign.get
      val fp = phase.rulesetImpl.fightProperties
      val duration = fp.timeLimit + fp.breakDuration + fp.timeBetweenFights
      ts.fights += f.schedule(ts.firstFreeMoment, duration)
      ts.save()
      f.save()
    }

    Reload
  }

  def unscheduleFight(sf: ScheduledFight[_]) = unscheduleFights(Seq[ScheduledFight[_]](sf))

  def unscheduleFights(sfs: Seq[ScheduledFight[_]]) = {
    for (sf <- sfs) {
      val ts = sf.timeslot.foreign.get
      sf.fight.foreign.foreach { f =>
        f.scheduled(Empty)
        f.save()
      }

      sf.delete_!

      ts.save()
    }

    Reload
  }

  def moveUp(sf: ScheduledFight[_]) = {
    sf.previous.foreach { prev =>
      val time = sf.time.is
      sf.time(prev.time.is)
      prev.time(time)
      sf.save()
      prev.save()
    }

    Reload
  }

  def moveDown(sf: ScheduledFight[_]) = {
    sf.next.foreach { next =>
      val time = sf.time.is
      sf.time(next.time.is)
      next.time(time)
      sf.save()
      next.save()
    }

    Reload
  }

  def pack(ts: ArenaTimeSlot) = {
    ts.fights.sortBy(_.time.is).toList match {
      case Nil => // do nothing
      case fights =>
        val globalOffset = ts.from.get - fights.head.time.get
        fights.head.time(fights.head.time.get + globalOffset)
        def connectToPrevious(fights: List[ScheduledFight[_]]): Unit = fights match {
          case f1 :: f2 :: rest =>
            f2.time(f1.time.get + f1.duration.get)
            connectToPrevious(f2 :: rest)
          case _ => ()
        }
        connectToPrevious(fights)
        ts.save()
    }
    Reload
  }

  def action(name: String, action: ScheduledFight[_] => JsCmd)(implicit sf: ScheduledFight[_]) =
    "a" #> SHtml.a(() => action(sf), Text(name))

  def action(name: String, action: () => JsCmd) =
    "a" #> SHtml.a(action, Text(name))

  def render = {
    val colspan = 9 / Arena.count.max(1)

    val divider = ("* [class+]" #> "divider" & "* *" #> Nil)

    def schedule(fights: Seq[Fight[_, _]]) = ".schedule" #> (
      ".arena" #> Arena.findAll().map(a =>
        ".arenaName *" #> a.name.get &
          ".day" #> Day.findAll().zipWithIndex.map {
            case (d, i) =>
              ".dayName *" #> ("Day " + (i + 1)) &
                ".timeslot" #> a.timeslots.filter(_.day.is == d.id.is).filter(_.fightingTime.is).map(ts =>
                  "a" #> SHtml.a(() => scheduleFights(fights, ts), <span class="from">{ df.format(ts.from.get) }</span><span class="to">{ df.format(ts.to.get) }</span><span class="name">{ ts.name.get }</span>))
          }))

    def renderFights(fights: Seq[Fight[_, _]]) = ".fight" #> fights.map(f =>
      ".name *" #> f.name.get &
        ".schedule" #> (
          ".arena" #> Arena.findAll().map(a =>
            ".arenaName *" #> a.name.get &
              ".day" #> Day.findAll().zipWithIndex.map {
                case (d, i) =>
                  ".dayName *" #> ("Day " + (i + 1)) &
                    ".timeslot" #> a.timeslots.filter(_.day.is == d.id.is).filter(_.fightingTime.is).map(ts =>
                      "a" #> SHtml.a(() => scheduleFight(f, ts), <span class="name">{ ts.name.get }</span><span class="from">{ df.format(ts.from.get) }</span><span class="to">{ df.format(ts.to.get) }</span>))
              })))

    def renderPhase(name: String, fights: Seq[Fight[_, _]]): Option[CssSel] = fights.isEmpty match {
      case false =>
        Some(".phaseHeader" #> (
          ".name *" #> name &
          schedule(fights)) &
          renderFights(fights))
      case true =>
        None
    }

    ".arena" #> Arena.findAll.map(a =>
      ".arena [class+]" #> ("col-md-" + colspan) &
        ".arenaName" #> a.name.get &
        ".download-schedule" #> SHtml.link("/download/participants", () => throw new ResponseShortcutException(downloadSchedule(a)), Text("Download schedule")) &
        ".timeslot" #> a.timeslots.map(ts =>
          ".header" #> (
            ".name *" #> ts.name.get &
            ".from *" #> df.format(ts.from.get) &
            ".to *" #> df.format(ts.to.get) &
            ".action" #> List(
              action("Pack", () => pack(ts)),
              divider,
              action("Unschedule", () => unscheduleFights(ts.fights)))) &
              ".fight" #> ts.fights.sortBy(_.time.is).map { implicit sf =>
                sf.fight.foreign match {
                  case Full(f) =>
                    implicit val p = f.phase.foreign.get
                    implicit val t = p.tournament.foreign.get
                    ".fight [class+]" #> (if (f.finished_?) "success" else "waiting") &
                      ".time *" #> df.format(new Date(sf.time.is)) &
                      ".tournament *" #> tournamentName &
                      ".name *" #> f.name.get &
                      ".action" #> List(
                        action("Move up", moveUp _),
                        action("Move down", moveDown _),
                        divider,
                        action("Unschedule", unscheduleFight _))
                  case _ =>
                    ".name *" #> "???"
                }
              })) &
      ".unscheduled" #> (".tournament" #> Tournament.findAll().map(t =>
        ".tournamentLabel *" #> t.mnemonic.get &
          (t.name.get match {
            case n if n.length > 20 =>
              ".tournamentName *" #> (n.take(20) + "...") &
                ".tournamentName [title]" #> t.name.get
            case n =>
              ".tournamentName *" #> n
          }) &
          ".unscheduledCount *" #> t.phases.flatMap(_.fights.filter(_.scheduled.foreign.isEmpty)).size &
          ".phase" #> (t.poolPhase.pools.map(pool =>
            renderPhase(s"Pool ${pool.poolName}", pool.fights.filter(_.scheduled.foreign.isEmpty)))
            ++ List(t.eliminationPhase, t.finalsPhase).map(p =>
              renderPhase(p.name.get, p.fights.filter(_.scheduled.foreign.isEmpty)))).flatten))
  }

  def tournamentName(implicit t: Tournament) =
    <a href={ "/tournaments/view/" + t.identifier.get } class="label label-default" title={ t.name.get }>
      { t.mnemonic.get }
    </a>

  def downloadSchedule(a: Arena, onlyUnfinishedPools: Boolean = true) = {
    OutputStreamResponse(ScheduleExporter.doExport(a, onlyUnfinishedPools) _, List("content-disposition" -> ("inline; filename=\"schedule_arena_" + a.id.get + ".xls\"")))
  }

}
