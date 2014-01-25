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

  val dayDateFormat = new SimpleDateFormat("MMMM d")
  dayDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))

  def schedulableFights(fs: Seq[Fight[_, _]]) = fs.filterNot(_.cancelled.is).filter(_.scheduled.foreign.isEmpty)

  def scheduleFight(f: Fight[_, _], ts: ArenaTimeSlot) = scheduleFights(Seq[Fight[_, _]](f), ts)

  def scheduleFights(fs: Seq[Fight[_, _]], ts: ArenaTimeSlot) = {
    schedulableFights(fs).foreach { f =>
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
      "* [id]" #> f.id.get &
        ".name" #> fightName(f) &
        ".schedule" #> (
          ".arena" #> Arena.findAll().map(a =>
            ".arenaName *" #> a.name.get &
              ".day" #> Day.findAll().zipWithIndex.map {
                case (d, i) =>
                  ".dayName *" #> ("Day " + (i + 1)) &
                    ".timeslot" #> a.timeslots.filter(_.day.is == d.id.is).filter(_.fightingTime.is).map(ts =>
                      "a" #> SHtml.a(() => scheduleFight(f, ts), <span class="from">{ df.format(ts.from.get) }</span><span class="to">{ df.format(ts.to.get) }</span><span class="name">{ ts.name.get }</span>))
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

    def fightInfo(f: Fight[_, _]): String = {
      def fighter(fighter: Fighter, side: String) = fighter.participant match {
        case Some(p) =>
          s"""<span class="badge $side">${p.subscription(f.tournament).get.fighterNumber.get}</span> ${p.name.get}"""
        case None =>
          s"""<span class="badge $side">?</span> ${fighter.toString}"""
      }

      fighter(f.fighterA, "red") + "<br/>" + fighter(f.fighterB, "blue")
    }

    def fightLabel(f: Fight[_, _]): String = {
      import nl.malienkolders.htm.admin.lib.Utils.TimeRenderHelper
      val classAndText: (String, String) = (f.cancelled.get match {
        case true =>
          "danger" -> "cancelled"
        case false =>
          f.finished_? match {
            case true =>
              val s = f.currentScore

              "success" -> s"${s.red} (${s.double}) ${s.blue}"
            case false =>
              (f.scheduled.foreign.map(_ => "info").getOrElse("warning")) -> (f.scheduled.foreign.map(sf => sf.time.get.hhmm).getOrElse("unscheduled"))
          }
      })
      val (styleClass, text) = classAndText
      s"""${f.name.get} <span class="pull-right label label-$styleClass">$text</span>"""
    }

    def fightName(f: Fight[_, _]) =
      "* *" #> f.name.get &
        "* [title]" #> fightLabel(f) &
        "* [rel]" #> "popover" &
        "* [data-content]" #> fightInfo(f)

    ".arena" #> Arena.findAll.map(a =>
      ".arena [class+]" #> ("col-md-" + colspan) &
        ".arenaName" #> a.name.get &
        ".download-schedule" #> SHtml.link("/download/schedule/arena", () => throw new ResponseShortcutException(downloadSchedule(a)), Text("Download schedule")) &
        ".days-panel-group [id]" #> ("arena-days-" + a.id.is.toString) &
        ".day" #> a.timeslotByDay.zipWithIndex.map {
          case (day, i) =>
            ".daydate" #> <a class="daydate" data-toggle="collapse" data-parent={ "#arena-days-" + a.id.is.toString } href={ "#arena-" + a.id.is.toString + "-day-" + day._1.id.is.toString }>{ s"Day ${i + 1}" } <small>| { dayDateFormat.format(new Date(day._1.date.get)) }</small></a> &
              ".panel-collapse [id]" #> ("arena-" + a.id.is.toString + "-day-" + day._1.id.is.toString) &
              ".panel-collapse [class+]" #> (if (now.getTime() < (day._1.date.is + 24.hours)) "in" else "out") &
              ".timeslot" #> day._2.map(ts =>
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
                          ".fight [id]" #> s"fight${f.id.get}" &
                            ".fight [class+]" #> (if (f.finished_?) "success" else "waiting") &
                            ".name" #> fightName(f) &
                            ".time *" #> df.format(new Date(sf.time.is)) &
                            ".tournament *" #> tournamentName &
                            ".action" #> List(
                              action("Move up", moveUp _),
                              action("Move down", moveDown _),
                              divider,
                              action("Unschedule", unscheduleFight _))
                        case _ =>
                          ".fight [class+]" #> "danger" &
                            ".time *" #> df.format(new Date(sf.time.is)) &
                            ".tournament *" #> "" &
                            ".name *" #> "Deleted fight" &
                            ".action" #> List(
                              action("Unschedule", unscheduleFight _))
                      }
                    })
        }) &
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
            renderPhase(s"Pool ${pool.poolName}", schedulableFights(pool.fights)))
            ++ List(t.eliminationPhase, t.finalsPhase).map(p =>
              renderPhase(p.name.get, schedulableFights(p.fights)))).flatten))
  }

  def tournamentName(implicit t: Tournament) =
    <a href={ "/tournaments/view/" + t.identifier.get } class="label label-default" title={ t.name.get }>
      { t.mnemonic.get }
    </a>

  def downloadSchedule(a: Arena, onlyUnfinishedTimeslots: Boolean = true) = {
    OutputStreamResponse(ScheduleExporter.doExport(a, onlyUnfinishedTimeslots) _, List("content-disposition" -> ("inline; filename=\"schedule_arena_" + a.id.get + ".xls\"")))
  }

}
