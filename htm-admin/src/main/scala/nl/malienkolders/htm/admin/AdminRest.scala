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
import nl.malienkolders.htm.lib.util.Helpers

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
      Extraction.decompose(Arena.findByKey(arenaId).map(_.scheduledFights.map(_.toMarshalledSummary)).getOrElse(false))

    case "api" :: "participants" :: Nil JsonGet _ =>
      Extraction.decompose(Participant.findAll.map(_.toMarshalled))

    case "api" :: "countries" :: Nil JsonGet _ =>
      Extraction.decompose(Country.findAll(By(Country.hasViewerFlag, true)).map(_.toMarshalled))

    case "api" :: "tournaments" :: Nil JsonGet _ =>
      Extraction.decompose(Tournament.findAll.map(_.toMarshalled))

    case "api" :: "tournament" :: AsLong(tournamentId) :: Nil JsonGet _ =>
      Tournament.findByKey(tournamentId).map(t => Extraction.decompose(t.toMarshalled)).getOrElse[JValue](JBool(false))

    case "api" :: "tournament" :: tournamentIdentifier :: "gearcheck" :: AsLong(participantId) :: Nil JsonPost json -> _ =>
      JBool(json match {
        case JBool(b) =>
          val result = for {
            t <- Tournament.find(By(Tournament.identifier, tournamentIdentifier))
            s <- t.subscriptions.find(_.participant.is == participantId)
          } yield {
            s.gearChecked(b).save()
            true
          }
          result.getOrElse(false)
        case _ => false
      })

    case "api" :: "pool" :: AsLong(poolId) :: Nil JsonGet _ =>
      Extraction.decompose(Pool.findByKey(poolId).map(_.toMarshalled).getOrElse(false))

    case "api" :: "pool" :: AsLong(poolId) :: "summary" :: Nil JsonGet _ =>
      Extraction.decompose(Pool.findByKey(poolId).map(_.toMarshalledSummary).getOrElse(false))

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
      RefreshServer.notifyClients
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
      val fight = Extraction.extract[MarshalledFight](json)
      FightServer ! FightUpdate(fight)
      JBool(true)

    case "api" :: "fight" :: "postpone" :: Nil JsonPost json -> _ =>
      val fight = Extraction.extract[MarshalledFight](json)
      FightServer ! PostponeFight(fight)
      RefreshServer.notifyClients
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

    case "api" :: "participant" :: AsLong(participantId) :: "present" :: Nil JsonPost json -> _ =>
      JBool(json match {
        case JBool(present) =>
          Participant.findByKey(participantId) match {
            case Full(p) =>
              p.isPresent(present).save()
              true
            case _ => false
          }
        case _ => false
      })

    case "api" :: "ping" :: Nil JsonGet _ =>
      JString("pong")

    case "api" :: "viewers" :: Nil JsonGet _ =>
      Extraction.decompose(Viewer.findAll.map(_.toMarshalled).toList)

    case "api" :: "viewer" :: "update" :: Nil JsonPost json -> _ =>
      JBool(json match {
        case JObject(JField("view", JString(view)) :: JField("viewers", JArray(viewers)) :: JField("payload", payload) :: Nil) =>
          def sendPayload(p: JValue) = viewers.map(_ match { case JInt(id) => id.toLong case _ => -1 }).filter(_ > -1).foreach { viewerId =>
            Viewer.findByKey(viewerId).foreach(viewer =>
              viewer.rest.update(view, p))
          }

          view match {
            case "overview/arena" =>
              payload match {
                case p: JObject =>
                  (p \\ "arenaId") match {
                    case JInt(arenaId) =>
                      Arena.findByKey(arenaId.toLong) match {
                        case Full(arena) =>
                          val fights = arena.scheduledFights.filterNot(_.fight.foreign.get.finished_?).map(_.toMarshalledSummary)
                          val newPayload = p ~ ("fights" -> Extraction.decompose(fights))
                          sendPayload(newPayload)
                          true
                        case _ => false
                      }
                    case _ => false
                  }
                case _ =>
                  false
              }
            case "overview/pool" =>
              payload match {
                case p: JObject =>
                  (p \\ "tournamentId") match {
                    case JInt(tournamentId) =>
                      Tournament.findByKey(tournamentId.toLong) match {
                        case Full(tournament) =>
                          val ruleset = tournament.poolPhase.rulesetImpl
                          val pools = ruleset.ranking(tournament.poolPhase)
                          val marshalledPools = pools.map {
                            case (pool, participants) =>
                              Extraction.decompose(pool.toMarshalledSummary).asInstanceOf[JObject] ~
                                ("participants" -> participants.map {
                                  case (participant, scores) =>
                                    Extraction.decompose(participant.subscription(tournament).get.toMarshalled).asInstanceOf[JObject] ~
                                      ("scores" -> scores.fields.map(f => ("name" -> f.name) ~ ("header" -> f.header.toString) ~ ("value" -> f.value())).toList)
                                })
                          }
                          val newPayload = ("tournament" -> Extraction.decompose(tournament.toMarshalledSummary)) ~
                            ("pools" -> marshalledPools)
                          sendPayload(newPayload)
                          true
                        case _ => false
                      }
                    case _ => false
                  }
                case _ => false
              }
            case "overview/selected_participants" =>
              payload match {
                case p: JObject =>
                  (p \\ "tournamentId") match {
                    case JInt(tournamentId) =>
                      val fighters: List[Fighter] = Tournament.findByKey(tournamentId.toLong).get.eliminationPhase.fights.filter(_.round.is == 1).map(f => List(f.fighterA, f.fighterB)).flatten.toList
                      val newPayload = p ~ ("fighters" -> Extraction.decompose(fighters))
                      sendPayload(newPayload)
                      true
                    case _ => false
                  }
                case _ =>
                  false
              }

            case _ =>
              sendPayload(payload)
              true
          }
        case _ =>
          false
      })

    case "api" :: "images" :: Nil JsonGet _ =>
      Extraction.decompose(Image.findAll.map(_.toMarshalled).toList)

    case "api" :: "qr" :: Nil JsonGet req =>
      InMemoryResponse(Helpers.generateQrImage, List("Content-Type" -> "image/png"), Nil, 200)

  }

}