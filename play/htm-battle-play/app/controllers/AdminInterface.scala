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

  def pool(id: Long) = Action.async {
    WS.url("http://localhost:8079/api/pool/" + id).get.map(response => Ok(response.json))
  }

  def fight(id: Long) = Action.async {
    WS.url("http://localhost:8079/api/fight/" + id).get.map(response => Ok(response.json))
  }

  def fightUpdate = Action.async(parse.json) { req =>
    WS.url("http://localhost:8079/api/fight/update").post(req.body).map(_ => Ok)
  }

  def arenas = Action.async {
    WS.url("http://localhost:8079/api/arenas").get.map(response => Ok(response.json))
  }

  def arena(id: Long) = Action.async {
    WS.url("http://localhost:8079/api/arena/" + id + "/pools").get.map(response => Ok(response.json))
  }

  def poolFight(poolId: Long, fightOrder: Long) = Action.async {
    WS.url("http://localhost:8079/api/pool/" + poolId + "/fight/" + fightOrder).get.map(response => Ok(response.json))
  }

}