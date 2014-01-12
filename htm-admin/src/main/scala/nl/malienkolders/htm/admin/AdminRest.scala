package nl.malienkolders.htm.admin

import net.liftweb._
import http._
import rest._
import json._
import JsonDSL._
import mapper._
import util.Helpers._
import nl.malienkolders.htm.lib.model._
import nl.malienkolders.htm.admin.comet._
import net.liftweb.common.Full
import nl.malienkolders.htm.admin.lib.exporter.JsonFightExporter

object AdminRest extends RestHelper {

  override implicit val formats = Serialization.formats(NoTypeHints)

  serve {
    case "api" :: "v1" :: "status" :: "all" :: Nil JsonGet _ =>
      JsonFightExporter.createExport

    case "api" :: "event" :: Nil JsonGet _ =>
      JString(Event.theOne.name.get)

    case "api" :: "arenas" :: Nil JsonGet _ =>
      Extraction.decompose(Arena.findAll.map(_.toMarshalled))

    case "api" :: "arena" :: AsLong(arenaId) :: Nil JsonGet _ =>
      Extraction.decompose(Arena.findByKey(arenaId).map(_.fights.filter(_.fight.foreign.isDefined).map(_.toMarshalledSummary)).getOrElse(false))

    case "api" :: "participants" :: Nil JsonGet _ =>
      Extraction.decompose(Participant.findAll.map(_.toMarshalled))

    case "api" :: "countries" :: Nil JsonGet _ =>
      Extraction.decompose(Country.findAll(By(Country.hasViewerFlag, true)).map(_.toMarshalled))

    case "api" :: "tournaments" :: Nil JsonGet _ =>
      Extraction.decompose(Tournament.findAll.map(_.toMarshalled))

    case "api" :: "tournament" :: AsLong(tournamentId) :: Nil JsonGet _ =>
      Tournament.findByKey(tournamentId).map(t => Extraction.decompose(t.toMarshalled)).getOrElse[JValue](JBool(false))

    case "api" :: "pool" :: AsLong(poolId) :: Nil JsonGet _ =>
      Extraction.decompose(Pool.findByKey(poolId).map(_.toMarshalled).getOrElse(false))

    case "api" :: "pool" :: AsLong(poolId) :: "summary" :: Nil JsonGet _ =>
      Extraction.decompose(Pool.findByKey(poolId).map(_.toMarshalledSummary).getOrElse(false))

    case "api" :: "pool" :: AsLong(poolId) :: "viewer" :: Nil JsonGet _ =>
      val p = Pool.findByKey(poolId)
      var j = p.map(p => Extraction.decompose(p.toViewer)).getOrElse[JValue](JBool(false))
      JsonResponse(
        j,
        ("Content-Type", "application/json; charset=utf-8") :: Nil,
        Nil,
        200)

    case "api" :: "pool" :: AsLong(poolId) :: "fight" :: "pop" :: Nil JsonGet _ =>
      (FightServer !! PopFight(Pool.findByKey(poolId).get)).map {
        case FightMsg(f) => Extraction.decompose(f.toMarshalled)
        case _ => JBool(false)
      }.getOrElse[JValue](JBool(false))

    case "api" :: "fight" :: "confirm" :: Nil JsonPost json -> _ =>
      val m = Extraction.extract[MarshalledFight](json)
      JBool((FightServer !! FightResult(FightHelper.dao(m.phaseType).findByKey(m.id).get.fromMarshalled(m).asInstanceOf[Fight[_, _]], true)).map {
        case FightMsg(_) => true
        case _ => false
      }.getOrElse[Boolean](false))

    case "api" :: "fight" :: "cancel" :: Nil JsonPost json -> _ =>
      val m = Extraction.extract[MarshalledFight](json)
      JBool((FightServer !! FightResult(FightHelper.dao(m.phaseType).findByKey(m.id).get.fromMarshalled(m).asInstanceOf[Fight[_, _]], false)).map {
        case FightMsg(_) => true
        case _ => false
      }.getOrElse[Boolean](false))

    case "api" :: "fight" :: phase :: AsLong(id) :: Nil JsonGet _ =>
      val dao = phase match {
        case "P" => PoolFight
        case "E" => EliminationFight
        case _ => PoolFight
      }
      dao.findByKey(id).map(f => Extraction.decompose(f.toMarshalled)).getOrElse[JValue](JBool(false))

    case "api" :: "fight" :: "update" :: Nil JsonPost json -> _ =>
      val fight = Extraction.extract[MarshalledFightSummary](json)
      FightServer ! FightUpdate(fight)
      JBool(true)

    case "api" :: "fight" :: "postpone" :: Nil JsonPost json -> _ =>
      val fight = Extraction.extract[MarshalledFightSummary](json)
      FightServer ! PostponeFight(fight)
      JBool(true)

    case "api" :: "fight" :: "update" :: phase :: AsLong(id) :: "timer" :: Nil JsonPost json -> _ =>
      val timer = Extraction.extract[TimerMessage](json)
      FightServer ! TimerUpdate(FightId(phase, id), timer)
      JBool(true)

    case "api" :: "fight" :: "update" :: phase :: AsLong(id) :: "message" :: Nil JsonPost json -> _ =>
      json match {
        case JString(message) =>
          FightServer ! MessageUpdate(FightId(phase, id), message)
          JBool(true)
        case _ => JBool(false)
      }

    case "api" :: "ping" :: Nil JsonGet _ =>
      JString("pong")

    case "api" :: "viewers" :: Nil JsonGet _ =>
      Extraction.decompose(Viewer.findAll.map(_.toMarshalled).toList)

    case "api" :: "viewer" :: "update" :: Nil JsonPost json -> _ =>
      JBool(json match {
        case JObject(JField("view", JString(view)) :: JField("viewers", JArray(viewers)) :: JField("payload", payload) :: Nil) =>
          if (view == "overview/arena") {
            payload match {
              case p: JObject =>
                (p \\ "arenaId") match {
                  case JInt(arenaId) =>
                    Arena.findByKey(arenaId.toLong) match {
                      case Full(arena) =>
                        val fights = arena.fights.filterNot(_.fight.foreign.get.finished_?).map(_.toMarshalledSummary)
                        val newPayload = p ~ ("fights" -> Extraction.decompose(fights))
                        viewers.map(_ match { case JInt(id) => id.toLong case _ => -1 }).filter(_ > -1).foreach { viewerId =>
                          Viewer.findByKey(viewerId).foreach(viewer =>
                            viewer.rest.update(view, newPayload))
                        }
                        true
                      case _ => false
                    }
                  case _ => false
                }
              case _ =>
                false
            }

          } else {
            viewers.map(_ match { case JInt(id) => id.toLong case _ => -1 }).filter(_ > -1).foreach { viewerId =>
              Viewer.findByKey(viewerId).foreach(viewer =>
                viewer.rest.update(view, payload))
            }
            true
          }
        case _ =>
          false
      })

    case "api" :: "images" :: Nil JsonGet _ =>
      Extraction.decompose(Image.findAll.map(_.toMarshalled).toList)
  }

}