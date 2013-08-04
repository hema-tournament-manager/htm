package nl.malienkolders.htm.battle.snippet

import net.liftweb._
import common._
import http._
import dispatch._
import js._
import JsCmds._
import util.Helpers._

object ShowAdminConnection {

  var adminHost = "localhost:8079"

  def render = {
    def isAlive = {
      val req = :/(adminHost) / "api" / "ping"
      val res = Http(req OK as.String).fold[Boolean](
        _ => false,
        success => success.contains("pong")).apply
      println(res)
      res
    }

    "name=server" #> SHtml.ajaxText(adminHost, { s =>
      adminHost = s
      if (isAlive) {
        S.notice("Admin host changed and alive")
        Run("$('#adminServerState').addClass('online').removeClass('offline');")
      } else {
        S.notice("Admin host cannot be pinged")
        Run("$('#adminServerState').addClass('offline').removeClass('online');")
      }
    }) &
      "#adminServerState [class]" #> (if (isAlive) "online" else "offline")
  }

}