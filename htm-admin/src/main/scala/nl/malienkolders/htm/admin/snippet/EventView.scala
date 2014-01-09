package nl.malienkolders.htm.admin.snippet

import net.liftweb._
import http._
import mapper._
import util.Helpers._
import scala.xml.NodeSeq
import nl.malienkolders.htm.lib.model._
import com.github.nscala_time.time.Imports._

class EventView {
  val datef = DateTimeFormat.forPattern("yyyy-MM-dd").withZone(DateTimeZone.UTC)
  val fmt = DateTimeFormat.forPattern("HH:mm").withZone(DateTimeZone.UTC)

  def timeslotMatrix = {
    val timeslotsPerArena = ArenaTimeSlot.findAll().groupBy(_.arena.is)
    val arenaIds = Arena.findAll().map(_.id.get)
    def loop(map: Map[Long, List[ArenaTimeSlot]]): List[List[Option[ArenaTimeSlot]]] = map.values.exists(!_.isEmpty) match {
      case false => arenaIds.map(_ => Nil)
      case true =>
        arenaIds.map(a => map.get(a).map(_.headOption).getOrElse(None)) :: loop(arenaIds.map(a => a -> map.get(a).map(_ match { case Nil => Nil case x => x.tail }).getOrElse(Nil)).toMap)
    }
    loop(timeslotsPerArena)
  }

  def timeInput(t: ArenaTimeSlot, f: MappedLong[_]) = SHtml.text(LocalTime.fromMillisOfDay(f.get).toString(fmt), s => {
    f(fmt.parseLocalTime(s).millisOfDay().get)
    t.save()
  }, "class" -> "form-control input-sm")

  def render = {
    val e = Event.findAll().headOption.getOrElse(Event.create.name("My Event " + DateTime.now.year().get()).saveMe())
    val ds = Day.findAll() match {
      case Nil => Day.create.date(LocalDate.now.toDate.getTime).saveMe() :: Nil
      case days => days
    }
    val arenas = Arena.findAll()
    ".eventName *" #> e.name.get &
      "#saveAll" #> SHtml.button(<span><span class="glyphicon glyphicon-floppy-disk"/> Save changes</span>, () => e.save()) &
      ".day" #> ds.zipWithIndex.map {
        case (d, i) =>
          ".dayTitle *" #> ("Day " + (i + 1)) &
            ".date" #> SHtml.text(new LocalDate(d.date.get).toString(datef), s => d.date(datef.parseLocalDate(s).toDate().getTime()).save(), "class" -> "date pull-right form-control input-sm") &
            ".arenaHeader" #> arenas.map(a => "* *" #> a.name.get) &
            ".timeslot" #> timeslotMatrix.map(ts =>
              ".arena" #> ts.map {
                case Some(t) =>
                  ".from *" #> timeInput(t, t.from) &
                    ".to *" #> timeInput(t, t.to) &
                    ".name *" #> SHtml.text(t.name.get, s => t.name(s).save(), "class" -> "form-control") &
                    ".remove" #> SHtml.button(<span class="glyphicon glyphicon-trash"></span>, () => t.delete_!)
                case _ => "* *" #> Nil
              }) &
            ".newTimeslot" #> (".arena" #> arenas.map(a =>
              "button" #> SHtml.button(<span><span class="glyphicon glyphicon-plus"></span> Add timeslot</span>, () => {
                a.timeslots += ArenaTimeSlot.create
                  .day(d)
                  .from(new LocalTime(10, 0).millisOfDay().get())
                  .to(new LocalTime(18, 0).millisOfDay().get())
                  .name("Block")
                  .fightingTime(true)
                a.save()
              })))
      }
  }
}