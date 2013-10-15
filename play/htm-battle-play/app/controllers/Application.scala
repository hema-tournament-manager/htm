package controllers

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Akka
import akka.actor.Actor
import akka.pattern.{ ask, pipe }
import akka.util.Timeout
import net.liftweb.json._
import play.api.libs.json.JsNumber
import nl.malienkolders.htm.lib.model.MarshalledPoolSummary
import akka.actor.Props
import actors.BattleServer
import actors.BattleServerMsgs._
import nl.malienkolders.htm.lib.model.MarshalledRound
import nl.malienkolders.htm.lib.model.MarshalledFight
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import nl.malienkolders.htm.lib.model.MarshalledFight

object Application extends Controller {

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global

  implicit val formats = Serialization.formats(NoTypeHints)

  implicit val timeout = Timeout(10000)

  lazy val battleServer = Akka.system(Play.current).actorOf(Props[BattleServer], "battleServer");

  def index = Action {
    Ok(views.html.index(Play.current.configuration.getString("app.name").get, Play.current.configuration.getString("app.version").get))
  }

  def fight = Action {
    Ok(views.html.fight(Play.current.configuration.getString("app.name").get))
  }

  def poolSelection = Action {
    Ok(views.html.pools(Play.current.configuration.getString("app.name").get))
  }

  def currentPoolId = Action.async {
    val currentPool = for (p <- (battleServer ? RequestCurrentPool).mapTo[Option[MarshalledPoolSummary]]) yield p
    currentPool.map(op => op.map(p => Ok(JsNumber(p.id))).getOrElse(BadRequest))
  }

  def subscribe = Action { request =>
    request.body.asJson.map { json =>
      val pool = Serialization.read[MarshalledPoolSummary](json.toString)
      battleServer ! SubscribePool(pool)
      battleServer ! SetCurrentFight(pool)
      Ok("Subscribed to pool " + pool.id)
    }.getOrElse(BadRequest)
  }

  def currentRound = Action.async {
    val f = for (c <- (battleServer ? RequestCurrentFight).mapTo[(Option[MarshalledRound], Option[MarshalledPoolSummary])]) yield c
    f.map { case (or, _) => or.map(r => Ok(Serialization.write(r))).getOrElse(BadRequest) }
  }

  def currentPool = Action.async {
    val f = for (c <- (battleServer ? RequestCurrentFight).mapTo[(Option[MarshalledRound], Option[MarshalledPoolSummary])]) yield c
    f.map { case (_, op) => op.map(p => Ok(Serialization.write(p))).getOrElse(BadRequest) }
  }

  def fightUpdate = Action { request =>
    request.body.asJson.map { json =>
      val fight = Serialization.read[MarshalledFight](json.toString)
      battleServer ! FightUpdate(fight)
      Ok("Updated fight")
    }.getOrElse(BadRequest)
  }

  def jsRoutes(varName: String = "jsRoutes") = Action { implicit request =>
    Ok(
      Routes.javascriptRouter(varName)(
        routes.javascript.AdminInterface.fight,
        routes.javascript.AdminInterface.pool,
        routes.javascript.AdminInterface.round,
        routes.javascript.AdminInterface.tournaments,
        routes.javascript.Application.currentRound,
        routes.javascript.Application.currentPool,
        routes.javascript.Application.currentPoolId,
        routes.javascript.Application.fight,
        routes.javascript.Application.fightUpdate,
        routes.javascript.Application.subscribe)).as(JAVASCRIPT)
  }

}