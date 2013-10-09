package controllers

import play.api._
import play.api.mvc._
import play.api.libs.ws._
import scala.concurrent.Future

object AdminInterface extends Controller {

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global

  def tournaments = Action.async {
    WS.url("http://localhost:8079/api/tournaments").get.map(response => Ok(response.json))
  }
  
  def round(id: Long) = Action.async {
    WS.url("http://localhost:8079/api/round/" + id).get.map(response => Ok(response.json))
  }

}