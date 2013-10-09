package controllers

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Akka
import akka.actor.Actor
import actors.RequestCurrentPool
import akka.pattern.{ ask, pipe }
import akka.util.Timeout
import net.liftweb.json._
import play.api.libs.json.JsNumber
import actors.SubscribePool
import nl.malienkolders.htm.lib.model.MarshalledPoolSummary
import akka.actor.Props
import actors.BattleServer

object Application extends Controller {

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global

  implicit val formats = Serialization.formats(NoTypeHints)

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

  def currentPool = Action.async {
    implicit val timeout = Timeout(10000)
    
    val currentPool = for (p <- (battleServer ? RequestCurrentPool).mapTo[Option[MarshalledPoolSummary]]) yield p
    currentPool.map(op => op.map(p => Ok(JsNumber(p.id))).getOrElse(BadRequest))
  }

  def subscribe = Action { request =>
    request.body.asJson.map { json =>
      val pool = Serialization.read[MarshalledPoolSummary](json.toString)
      battleServer ! SubscribePool(pool)
      Ok("Subscribed to pool " + pool.id)
    }.getOrElse(BadRequest)
  }
  
  def jsRoutes(varName: String = "jsRoutes") = Action { implicit request =>
    Ok(
      Routes.javascriptRouter(varName)(
        routes.javascript.AdminInterface.tournaments,
        routes.javascript.AdminInterface.round,
        routes.javascript.Application.currentPool,
        routes.javascript.Application.subscribe)).as(JAVASCRIPT)
  }

}