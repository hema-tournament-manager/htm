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
import javax.imageio.ImageIO
import play.api.Play.current

object Application extends Controller {

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global

  implicit val timeout = Timeout(10000)

  val (updateOut, updateChannel) = Concurrent.broadcast[JsValue]

  var clientState = Json.obj(
    "empty" -> Json.obj(),
    "image" -> Json.obj(),
    "fight" -> Json.obj(),
    "overview/arena" -> Json.obj(),
    "participant/footer" -> Json.obj(),
    "participant/bio" -> Json.obj(
      "side" -> "left",
      "participant" -> Json.obj(
        "name" -> "Name",
        "club" -> "Club")));

  case class TimerInfo(battleTime: Long, viewerTime: Long, action: String);

  var timer = TimerInfo(0, System.currentTimeMillis(), "stop");

  def index = Action {
    Ok(views.html.index())
  }

  def view(resolution: String) = Action {
    Ok(views.html.view(Resolution.fromString(resolution)))
  }

  def ping = Action { Ok("true") }

  private def updateImpl(msg: JsValue) = {
    val view = (msg \ "view").as[String]
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

    val viewState = (clientState \ view).asInstanceOf[JsObject] ++ payload;

    val replacePayload = (__ \ view).json.put(viewState)

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

  private def generateThresholded(filename: String): String = {
    val newFilename = filename.replace(".png", "-thresholded.png");

    if (new File(newFilename).exists()) {
      return newFilename;
    }

    val image = ImageIO.read(new File(filename))

    val alpha = image.getAlphaRaster()

    for {
      x <- 0 to image.getWidth() - 1
      y <- 0 to image.getHeight() - 1
    } {
      val pixel = Array[Int](0)
      alpha.getPixel(x, y, pixel);

      if (pixel(0) > 127) {
        pixel.update(0, 255)
      } else {
        pixel.update(0, 0)
      }

      alpha.setPixel(x, y, pixel)
    }

    ImageIO.write(image, "png", new File(newFilename))

    return newFilename;

  }

  def photo(id: String, side: String) = Action {
    val photoFile = new File("Avatars/Generated/" + id + "_default_" + side + ".jpg")
    if (photoFile.exists()) {
      Ok.sendFile(photoFile)
    } else
      Redirect(routes.Assets.at("images/placeholder_default_" + side + ".png"))
  }

  def image(resolution: String, name: String) = Action {
    val imageFile = new File(s"Images/$resolution/$name")

    if (imageFile.exists()) {

      if (resolution != "1024x768") {
        Ok.sendFile(new File(generateThresholded(imageFile.toString())));
      }
      Ok.sendFile(imageFile)
    } else
      Redirect(routes.Assets.at("images/empty.png"))
  }

  def publicImage(name: String) = Action { request =>

    val imageFile = new File(current.path.getAbsolutePath + s"/public/images/$name")

    println(request.getQueryString("thresholding"))

    if (request.getQueryString("thresholding").map(_ == "yes").getOrElse(false)) {
      Ok.sendFile(new File(generateThresholded(imageFile.toString())));
    } else {
      Ok.sendFile(imageFile);
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