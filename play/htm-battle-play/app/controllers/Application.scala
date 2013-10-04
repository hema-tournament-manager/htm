package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {

  def index = Action {
    Ok(views.html.index(Play.current.configuration.getString("app.name").get, Play.current.configuration.getString("app.version").get))
  }
  
  def jsRoutes(varName: String = "jsRoutes") = Action { implicit request =>
    Ok(
      Routes.javascriptRouter(varName)(
        )).as(JAVASCRIPT)
  }

}