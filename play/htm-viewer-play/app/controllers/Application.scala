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

  val (switchOut, switchChannel) = Concurrent.broadcast[String]
  val (fightOut, fightChannel) = Concurrent.broadcast[JsValue]

  def index = Action {
    Ok(views.html.index())
  }

  def switch(view: String) = Action {
    switchChannel.push(view)
    Ok
  }

  def switchFeed = Action {
    Ok.chunked(switchOut &> EventSource()).as("text/event-stream")
  }

  def fightUpdate = Action(parse.json) { req =>
    fightChannel.push(req.body)
    Ok
  }

  def fightFeed = Action {
    Ok.chunked(fightOut &> EventSource()).as("text/event-stream")
  }

  def jsRoutes(varName: String = "jsRoutes") = Action { implicit request =>
    Ok(
      Routes.javascriptRouter(varName)(
        routes.javascript.Application.index)).as(JAVASCRIPT)
  }

}