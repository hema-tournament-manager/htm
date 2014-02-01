package controllers

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Akka
import akka.actor.Actor
import akka.actor.Props
import akka.pattern.{ ask, pipe }
import akka.util.Timeout
import nl.malienkolders.htm.lib.model._
import play.api.libs.iteratee.Concurrent
import play.api.libs.EventSource
import play.api.libs.json._
import play.api.libs.Files
import play.api.libs.Files._
import _root_.lib.Unzipper
import java.io.File
import play.api.Play.current
import lib.ImageUtil
import play.api.libs.iteratee.Enumerator

object Application extends Controller {

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global

  implicit val timeout = Timeout(10000)

  val (updateOut, updateChannel) = Concurrent.broadcast[JsValue]

  var clientState = Json.obj(
    "empty" -> Json.obj(),
    "image" -> Json.obj(),
    "fight" -> Json.obj(),
    "overview/arena" -> Json.obj(),
    "overview/pool" -> Json.obj(),
    "overview/selected_participants" -> Json.obj(),
    "participant/footer" -> Json.obj(),
    "participant/bio" -> Json.obj(
      "side" -> "left",
      "participant" -> Json.obj(
        "name" -> "Name",
        "club" -> "Club")));

  case class TimerInfo(battleTime: Long, viewerTime: Long, action: String);

  var timer = TimerInfo(0, System.currentTimeMillis(), "stop");
  var currentView = "empty"

  def index = Action {
    Ok(views.html.index())
  }

  def view(resolution: String) = Action {
    Ok(views.html.view(Resolution.fromString(resolution)))
  }

  def ping = Action { Ok("true") }

  private def updateImpl(msg: JsValue) = {
    val newView = (msg \ "view").as[String]

    currentView = if (newView.isEmpty()) currentView else newView;

    val payload = (msg \ "payload").asInstanceOf[JsObject]

    if (payload.keys.contains("timer")) {
      timer = TimerInfo(battleTime = (payload \ "timer" \ "time").as[Long], viewerTime = System.currentTimeMillis(), action = (payload \ "timer" \ "action").as[String]);
    }
    if (payload.keys.contains("fight")) {
      val fight = (payload \ "fight").asInstanceOf[JsObject]
      if (fight.keys.contains("timer")) {
        timer = TimerInfo(battleTime = (fight \ "timer" \ "time").as[Long], viewerTime = System.currentTimeMillis(), action = (fight \ "action").as[String]);
      }
    }

    println(clientState.toString());
    println("currentView: " + currentView);

    val viewState = (clientState \ currentView).asInstanceOf[JsObject] ++ payload;

    val replacePayload = (__ \ currentView).json.put(viewState)

    clientState = clientState ++ clientState.transform(replacePayload).get

    println(clientState.toString());

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
    val paddedId = ("0" * (4 - id.length)) + id
    val photoFile = new File("Avatars/Generated/" + paddedId + "_default_" + side + ".jpg")
    if (photoFile.exists()) {
      Ok.sendFile(photoFile)
    } else
      Redirect(routes.Assets.at("images/placeholder_default_" + side + ".png"))
  }

  def image(resolution: String, name: String) = Action {
    val imageFile = new File(s"Images/$resolution/$name")

    if (imageFile.exists()) {
      if (name.endsWith(".png") && resolution != "1024x768") {
        Ok.sendFile(ImageUtil.generateThresholded(imageFile).getOrElse(imageFile))
      } else {
        Ok.sendFile(imageFile)
      }
    } else
      Redirect(routes.Assets.at("images/empty.png"))
  }

  def publicImage(name: String) = Action { request =>

    val imageStream = Application.getClass().getResourceAsStream(s"/public/images/$name")
    val imageFile = new File(current.path.getAbsolutePath + s"/public/images/$name")

    println(request.getQueryString("thresholding"))

    if (name.endsWith(".png") && request.getQueryString("thresholding").map(_ == "yes").getOrElse(false)) {
      ImageUtil.generateThresholded(imageFile, imageStream) match {
        case Some(thresholdedImage) => Ok.sendFile(thresholdedImage)
        case None => if (imageFile.exists()) Ok.sendFile(imageFile) else { imageStream.reset(); Ok.chunked(Enumerator.fromStream(imageStream)); }
      }

    } else {
      Ok.chunked(Enumerator.fromStream(imageStream));
    }
  }

  def updateFeed = Action {
    Ok.chunked(updateOut &> EventSource()).as("text/event-stream")
  }

  def initialState = Action {
    val timerPart =
      Json.obj(
        "timer" -> Json.obj(
          "action" -> timer.action,
          "time" -> (if (timer.action.equals("stop")) timer.battleTime else (timer.battleTime + (System.currentTimeMillis() - timer.viewerTime)))))
    val result = clientState ++
      Json.obj(
        "fight"
          ->
          ((clientState \ "fight").asInstanceOf[JsObject] ++ timerPart))

    Ok(result)
  }

  def jsRoutes(varName: String = "jsRoutes") = Action { implicit request =>
    Ok(
      Routes.javascriptRouter(varName)(
        routes.javascript.Application.updateFeed)).as(JAVASCRIPT)
  }

}