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
import nl.malienkolders.htm.lib.model.MarshalledRound
import nl.malienkolders.htm.lib.model.MarshalledFight
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import nl.malienkolders.htm.lib.model.MarshalledFight

object Application extends Controller {

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global

  implicit val formats = Serialization.formats(NoTypeHints)

  implicit val timeout = Timeout(10000)

  def index = Action {
    Ok(views.html.index(Play.current.configuration.getString("app.name").get, Play.current.configuration.getString("app.version").get))
  }

  def jsRoutes(varName: String = "jsRoutes") = Action { implicit request =>
    Ok(
      Routes.javascriptRouter(varName)(
        routes.javascript.AdminInterface.arena,
        routes.javascript.AdminInterface.arenas,
        routes.javascript.AdminInterface.fight,
        routes.javascript.AdminInterface.fightUpdate,
        routes.javascript.AdminInterface.pool,
        routes.javascript.AdminInterface.poolFight,
        routes.javascript.AdminInterface.round,
        routes.javascript.AdminInterface.tournaments)).as(JAVASCRIPT)
  }

}