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
import nl.malienkolders.htm.admin.lib.Utils.PimpedParticipant

object AdminRest extends RestHelper {

  override implicit val formats = Serialization.formats(NoTypeHints)

  serve {
    
    case "api" :: "v3" :: "tournament" :: Nil JsonGet _ =>
      Extraction.decompose(Tournament.findAll.map(_.toMarshalledV3))

    case "api" :: "v3" :: "tournament" :: Nil JsonPost json -> _ =>
      val m = Extraction.extract[MarshalledTournament](json)
      val t = Tournament.create.name(m.name).mnemonic(m.memo)
      t.save()
      Extraction.decompose(t.toMarshalledV3)

    case "api" :: "v3" :: "participant" :: "totals" :: Nil JsonGet _ =>
      case class TotalsResponseV3(participants: Long, clubs: Long, countries: Long)
      Extraction.decompose(TotalsResponseV3(
        participants = Participant.count,
        clubs = Participant.findAllFields(Seq(Participant.club), Distinct(), OrderBy(Participant.club, Ascending)).length,
        countries = Participant.findAllFields(Seq(Participant.country), Distinct()).length))

    case "api" :: "v3" :: "participant" :: Nil JsonGet request =>
      val page = request.param("page").map(_.toInt).getOrElse(0)
      val itemsPerPage = request.param("items").map(_.toInt).getOrElse(20)
      val q = request.param("query")
      val order = request.param("order") match {
        case Full("ASC") =>
          Ascending
        case Full("DESC") =>
          Descending
        case _ =>
          Ascending
      }
      val orderBy = OrderBy(request.param("orderBy") match {
        case Full("externalId") =>
          Participant.externalId
        case Full("name") =>
          Participant.name
        case Full("shortName") =>
          Participant.shortName
        case Full("club") =>
          Participant.club
        case Full("clubCode") =>
          Participant.clubCode
        case _ =>
          Participant.name
      }, order)
      val defaultParams: Seq[QueryParam[Participant]] = Seq(orderBy, StartAt(page * itemsPerPage), MaxRows(itemsPerPage))
      val queryParams: Seq[QueryParam[Participant]] = q match {
        case Full(query) if query.trim().length() > 0 =>
          val cleanQuery = query.toLowerCase().replaceAll("[^a-z0-9 ]", "")

          def sqlToQueryParam(s: String): QueryParam[Participant] = BySql(s, IHaveValidatedThisSQL("jdijkstra", "2014-10-05"))
          val params: Seq[QueryParam[Participant]] = (for (queryPart <- cleanQuery.split(" ")) yield {
            if (queryPart forall Character.isDigit) {
              // only search for id when the query is a number
              By(Participant.externalId, queryPart)
            } else {
              val columns = List("name", "club", "clubCode")
              val likes = columns.map("LOWER(" + _ + s") LIKE '%$queryPart%'")
              sqlToQueryParam("(" + likes.mkString(" OR ") + s" OR country IN (SELECT id FROM countries WHERE LOWER(name) LIKE '%$queryPart%' OR  LOWER(code2) LIKE '%$queryPart%'))")
            }
          }).toSeq
          params
        case _ =>
          Seq()
      }
      val response =  Participant.findAll((queryParams ++ defaultParams): _*).map(p => p.toMarshalledV3.copy(hasPicture = p.hasAvatar))
      Extraction.decompose(response)
   
  case "api" :: "v3" :: "participant" :: Nil JsonPost json -> _ =>
      val m = Extraction.extract[MarshalledParticipantV3](json)
      val p = Participant.create
      p.fromMarshalledV3(m)
      p.save()
      Extraction.decompose(p.toMarshalledV3)

  case "api" :: "v3" :: "country" :: Nil JsonGet _ =>
      Extraction.decompose(Country.findAll().map(_.toMarshalled))      
 
  case "api" :: "v3" :: "club" :: Nil JsonGet _ =>
     val clubs = Participant.findAllFields(Seq(Participant.club, Participant.clubCode), Distinct(), OrderBy(Participant.club, Ascending))
 									.map(p => MarshalledClub(None, p.clubCode, p.club))
      Extraction.decompose(clubs)        
      
    //=== Old API's ==================================================
    
    case "api" :: "v1" :: "status" :: "all" :: Nil JsonGet _ =>
      JsonFightExporter.createExport

    case "api" :: "event" :: Nil JsonGet _ =>
      JString(Event.theOne.name.get)

    case "api" :: "arenas" :: Nil JsonGet _ =>
      Extraction.decompose(Arena.findAll.map(_.toMarshalled))

    case "api" :: "arena" :: AsLong(arenaId) :: Nil JsonGet _ =>
      Extraction.decompose(Arena.findByKey(arenaId).map { arena =>
        val firstDay = arena.scheduledFights.find(!_.fight.foreign.get.finished_?).map(_.timeslot.foreign.get.day.get).getOrElse(0)
        arena.scheduledFights.filter(_.timeslot.foreign.get.day.is == firstDay).map(_.toMarshalledSummary)
      }.getOrElse(false))

    case "api" :: "participants" :: Nil JsonGet _ =>
      Extraction.decompose(Participant.findAll.map(p => p.toMarshalled.copy(hasPicture = Some(p.hasAvatar))))

    case "api" :: "v2" :: "participants" :: "totals" :: Nil JsonGet _ =>
      case class TotalsResponse(participants: Long, clubs: List[String], countries: List[String])
      Extraction.decompose(TotalsResponse(
        participants = Participant.count,
        clubs = Participant.findAllFields(Seq(Participant.club), Distinct(), OrderBy(Participant.club, Ascending)).map(_.club.get),
        countries = Participant.findAllFields(Seq(Participant.country), Distinct()).map(_.country.foreign.get.name.get).sorted))

    case "api" :: "v2" :: "participants" :: Nil JsonGet request =>
      case class ParticipantResponse(count: Long, previous: Option[String], next: Option[String], participants: List[MarshalledParticipant])
      val page = request.param("page").map(_.toInt).getOrElse(0)
      val itemsPerPage = request.param("itemsPerPage").map(_.toInt).getOrElse(20)
      val q = request.param("q")
      val order = request.param("order") match {
        case Full("ASC") =>
          Ascending
        case Full("DESC") =>
          Descending
        case _ =>
          Ascending
      }
      val orderBy = OrderBy(request.param("orderBy") match {
        case Full("externalId") =>
          Participant.externalId
        case Full("name") =>
          Participant.name
        case Full("shortName") =>
          Participant.shortName
        case Full("club") =>
          Participant.club
        case Full("clubCode") =>
          Participant.clubCode
        case _ =>
          Participant.name
      }, order)
      val defaultParams: Seq[QueryParam[Participant]] = Seq(orderBy, StartAt(page * itemsPerPage), MaxRows(itemsPerPage))
      val queryParams: Seq[QueryParam[Participant]] = q match {
        case Full(query) if query.trim().length() > 0 =>
          val cleanQuery = query.toLowerCase().replaceAll("[^a-z0-9 ]", "")

          def sqlToQueryParam(s: String): QueryParam[Participant] = BySql(s, IHaveValidatedThisSQL("jdijkstra", "2014-10-05"))
          val params: Seq[QueryParam[Participant]] = (for (queryPart <- cleanQuery.split(" ")) yield {
            if (queryPart forall Character.isDigit) {
              // only search for id when the query is a number
              By(Participant.externalId, queryPart)
            } else {
              val columns = List("name", "club", "clubCode")
              val likes = columns.map("LOWER(" + _ + s") LIKE '%$queryPart%'")
              sqlToQueryParam("(" + likes.mkString(" OR ") + s" OR country IN (SELECT id FROM countries WHERE LOWER(name) LIKE '%$queryPart%' OR  LOWER(code2) LIKE '%$queryPart%'))")
            }
          }).toSeq
          params
        case _ =>
          Seq()
      }
      val count = Participant.count(queryParams: _*)
      val response = ParticipantResponse(
        participants = Participant.findAll((queryParams ++ defaultParams): _*).map(p => p.toMarshalled.copy(hasPicture = Some(p.hasAvatar))),
        count = count,
        previous = if (page > 0) Some(s"/api/v2/participants?page=${page - 1}&itemsPerPage=$itemsPerPage&orderBy=${orderBy.field.name}&order=${if (order == Ascending) "ASC" else "DESC"}") else None,
        next = if ((page + 1) * itemsPerPage < count) Some(s"/api/v2/participants?page=${page + 1}&itemsPerPage=$itemsPerPage") else None)
      Extraction.decompose(response)

    case "api" :: "participants" :: Nil JsonPost json -> _ =>
      val m = Extraction.extract[MarshalledParticipant](json)
      val p = Participant.create
      p.fromMarshalled(m)
      p.save()
      JObject(List(JField("id", p.id.get)))

    case "api" :: "participants" :: AsLong(participantId) :: Nil JsonPost json -> _ =>
      val m = Extraction.extract[MarshalledParticipant](json)
      JBool(Participant.findByKey(participantId) match {
        case Full(p) =>
          p.fromMarshalled(m).save()
          true
        case _ => false
      })

    case "api" :: "participants" :: AsLong(participantId) :: "haspicture" :: Nil JsonGet _ =>
      JBool(Participant.findByKey(participantId).map(_.hasAvatar).getOrElse(false))

    case "api" :: "participants" :: AsLong(participantId) :: "picture" :: Nil Post req =>
      for {
        uploadedFile <- req.uploadedFiles.headOption
        participant <- Participant.findByKey(participantId)
      } yield {
        nl.malienkolders.htm.admin.lib.PhotoImporterBackend.handlePhoto(participant, uploadedFile.fileStream)
        JBool(true)
      }

    case "api" :: "countries" :: Nil JsonGet _ =>
      Extraction.decompose(Country.findAll().map(_.toMarshalled))

    case "api" :: "tournaments" :: Nil JsonGet _ =>
      Extraction.decompose(Tournament.findAll.map(_.toMarshalled))

//    case "api" :: "tournaments" :: Nil JsonPost json -> _ =>
//      val m = Extraction.extract[MarshalledTournament](json)
//      val t = Tournament.create
//      t.name(m.name).identifier(m.identifier).mnemonic(m.memo)
//      t.save()
//      JObject(List(JField("id", t.id.get)))

    case "api" :: "tournament" :: AsLong(tournamentId) :: Nil JsonGet _ =>
      Tournament.findByKey(tournamentId).map(t => Extraction.decompose(t.toMarshalled)).getOrElse[JValue](JBool(false))

    case "api" :: "tournament" :: AsLong(tournamentId) :: "participants" :: Nil JsonGet _ =>
      Tournament.findByKey(tournamentId).map(t => Extraction.decompose(t.participants.map(_.toMarshalled)));

    case "api" :: "tournament" :: AsLong(tournamentId) :: "participants" :: AsLong(participantId) :: Nil Put _ =>
      JBool(Tournament.findByKey(tournamentId).map(_.addParticipant(participantId)).getOrElse(false));

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
      FightHelper.dao(phase).findByKey(id).map(f => Extraction.decompose(f.toMarshalled)).getOrElse[JValue](JBool(false))

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
            case "overview/selected" =>
              payload match {
                case p: JObject =>
                  (p \\ "tournamentId") match {
                    case JInt(tournamentId) =>
                      val t = Tournament.findByKey(tournamentId.toLong).get
                      val fighters: List[Fighter] = t.eliminationPhase.fights.filter(_.round.is == 1).map(f => List(f.fighterA, f.fighterB)).flatten.toList
                      val newPayload = p ~ ("tournament" -> Extraction.decompose(t.toMarshalledSummary)) ~ ("fighters" -> Extraction.decompose(fighters.map(_.toMarshalled(t))))
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