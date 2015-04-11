package nl.malienkolders.htm.lib
package model

import net.liftweb._
import mapper._
import common._
import util._
import Helpers._
import scala.xml._
import dispatch._
import Http._
import net.liftweb.json._
import scala.concurrent._
import ExecutionContext.Implicits.global
import java.io.File
import java.nio.charset.Charset

case class ViewerMessage(message: String, duration: Long)

case class MarshalledViewer(id: Long, alias: String, url: String, arenas: List[Long])

class Viewer extends LongKeyedMapper[Viewer] with IdPK with CreatedUpdated with ManyToMany {
  def getSingleton = Viewer

  object alias extends MappedPoliteString(this, 32)
  object url extends MappedString(this, 255)
  object arenas extends MappedManyToMany(ArenaViewers, ArenaViewers.viewer, ArenaViewers.arena, Arena)

  def toMarshalled = MarshalledViewer(
    id.get,
    alias.get,
    url.get,
    arenas.map(_.id.get).toList)

  object rest {
    var state = "empty"

    def baseRequest = :/(url.get) / "api"
    implicit val formats = Serialization.formats(NoTypeHints)

    def ping = {
      val req = baseRequest / "ping"
      Http(req OK as.String).fold(
        _ => false,
        success => true).apply
    }

    private def update(arena: Arena, screen: String, data: JValue): Boolean = {
      state = screen
      update(arena, data)
    }

    private def update(arena: Arena, data: JValue): Boolean = {
      val serialized = compact(render(JObject(
        JField("view", JString(state)) ::
          JField("arena", JInt(arena.id.get)) ::
          JField("payload", data) :: Nil)))
      println("Sending update: " + serialized)
      val req = dispatch.url("http://" + url.get + "/api/update/text").POST.
        setBody(serialized.getBytes(Charset.forName("UTF-8"))).
        addHeader("Content-Type", "text/plain; charset=utf-8")
      Http(req).fold[Boolean](
        _ => false,
        resp => resp.getResponseBody().toBoolean).apply
    }

    def update(view: String, data: JValue): Boolean = {
      state = view;
      val serialized = compact(render(JObject(
        JField("view", JString(state)) ::
          JField("payload", data) :: Nil)))
      println("Sending update: " + serialized)
      val req = dispatch.url("http://" + url.get + "/api/update/text").POST.
        setBody(serialized.getBytes(Charset.forName("UTF-8"))).
        addHeader("Content-Type", "text/plain; charset=utf-8")

      Http(req).fold[Boolean](
        _ => false,
        resp => resp.getResponseBody().toBoolean).apply
    }

    def fightUpdate(arena: Arena, data: JValue): Boolean = {
      update(arena, "fight", data)
    }

//    def fightUpdate[F <: Fight[_, _]](arena: Arena, f: F): Boolean = {
//      if (state != "fight") {
//        f match {
//          case f: PoolFight =>
//            fightUpdate(arena, Extraction.decompose(Map("poolSummary" -> f.pool.obj.get.toMarshalledSummary)))
//          case f: EliminationFight =>
//            fightUpdate(arena, Extraction.decompose(Map("eliminationSummary" -> f.phase.foreign.get.fights.map(_.toMarshalled))))
//          case f: FreeStyleFight =>
//            fightUpdate(arena, Extraction.decompose(Map("freestyleSummary" -> f.phase.foreign.get.fights.filter(_.round.get == f.round.get).map(_.toMarshalled))))
//        }
//      }
//      fightUpdate(arena, Extraction.decompose(f.toMarshalled))
//    }

    def message(arena: Arena, message: String): Boolean = update(arena, Extraction.decompose(
      Map("message" -> message)))

    def timerUpdate(arena: Arena, action: String, time: Long) = fightUpdate(arena, Extraction.decompose(
      Map(
        "timer" -> Map(
          "action" -> action,
          "time" -> time))))

    def push(file: File) =
      Http(dispatch.url("http://" + url.get + "/api/push") <<< file)
  }
}

object Viewer extends Viewer with LongKeyedMetaMapper[Viewer] with CRUDify[Long, Viewer] {
  override def dbTableName = "viewers"
}