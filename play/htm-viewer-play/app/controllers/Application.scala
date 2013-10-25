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
import play.api.libs.Files
import play.api.libs.Files._
import _root_.lib.Unzipper
import java.io.File

object Application extends Controller {

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global

  implicit val formats = Serialization.formats(NoTypeHints)

  implicit val timeout = Timeout(10000)

  val (updateOut, updateChannel) = Concurrent.broadcast[JsValue]

  def index = Action {
    Ok(views.html.index())
  }

  def view(resolution: String) = Action {
    Ok(views.html.view(Resolution.fromString(resolution)))
  }

  def ping = Action { Ok("true") }

  def updateImpl(msg: JsValue) = {
    updateChannel.push(msg)
    Ok("true")
  }

  def update = Action(parse.json) { req =>
    updateImpl(req.body)
  }

  def updateText = Action(parse.text) { req =>
    updateImpl(Json.parse(req.body))
  }

  def push = Action(parse.temporaryFile) { req =>
    req.body match {
      case t: TemporaryFile =>
        Unzipper.unzip(new File("."), t.file)
        Ok
      case _ => BadRequest("Could not receive")
    }
  }

  def photo(id: String, side: String) = Action {
    val photoFile = new File("Avatars/Generated/" + id + "_default_" + side + ".jpg")
    if (photoFile.exists())
      Ok.sendFile(photoFile)
    else
      Redirect(routes.Assets.at("images/placeholder_default_" + side + ".png"))
  }

  def image(resolution: String, name: String) = Action {
    val imageFile = new File(s"Images/$resolution/$name")
    if (imageFile.exists())
      Ok.sendFile(imageFile)
    else
      Redirect(routes.Assets.at("images/empty.png"))
  }

  def updateFeed = Action {
    Ok.chunked(updateOut &> EventSource()).as("text/event-stream")
  }

  def jsRoutes(varName: String = "jsRoutes") = Action { implicit request =>
    Ok(
      Routes.javascriptRouter(varName)(
        routes.javascript.Application.updateFeed)).as(JAVASCRIPT)
  }

}