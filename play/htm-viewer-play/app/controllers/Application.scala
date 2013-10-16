package controllers

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Akka
import akka.actor.Actor
import akka.actor.Props
import akka.pattern.{ ask, pipe }
import akka.util.Timeout
import net.liftweb.json._
import nl.malienkolders.htm.lib.model._
import play.api.libs.iteratee.Concurrent
import play.api.libs.EventSource
import play.api.libs.json._

object Application extends Controller {

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global

  implicit val formats = Serialization.formats(NoTypeHints)

  implicit val timeout = Timeout(10000)

  val (updateOut, updateChannel) = Concurrent.broadcast[JsValue]

  def index = Action {
    Ok(views.html.index())
  }
  
  def ping = Action { Ok }

  def update(view: String) = Action(parse.json) { req =>
    updateChannel.push(Json.obj(
      "view" -> view,
      "payload" -> req.body))
    Ok
  }

  def updateFeed = Action {
    Ok.chunked(updateOut &> EventSource()).as("text/event-stream")
  }

  def jsRoutes(varName: String = "jsRoutes") = Action { implicit request =>
    Ok(
      Routes.javascriptRouter(varName)(
        routes.javascript.Application.index)).as(JAVASCRIPT)
  }

}