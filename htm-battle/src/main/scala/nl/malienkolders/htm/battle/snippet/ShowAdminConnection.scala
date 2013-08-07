package nl.malienkolders.htm.battle.snippet

import net.liftweb._
import common._
import http._
import dispatch._
import js._
import JsCmds._
import util.Helpers._
import nl.malienkolders.htm.battle.model.AdminServer
import net.liftweb.mapper.{ OrderBy, Descending }

object ShowAdminConnection {

  var adminHostText: String = _

  def adminHost = {
    AdminServer.find(OrderBy(AdminServer.createdAt, Descending)).map { as => as.hostname.get + ":" + as.port.get } getOrElse ("localhost:8079")
  }

  def adminHost(host: String) {
    val as = new AdminServer
    val hostPort = host.split(":")
    as.hostname := hostPort.head
    as.port := hostPort.tail.headOption.map(_.toInt).getOrElse(8079)
    as.save
    adminHostText = as.hostname.get + ":" + as.port.get
  }

  def render = {
    def isAlive = {
      val req = :/(adminHost) / "api" / "ping"
      val res = Http(req OK as.String).fold[Boolean](
        _ => false,
        success => success.contains("pong")).apply
      println(res)
      res
    }

    adminHostText = adminHost

    "name=server" #> SHtml.ajaxText(adminHostText, { s =>
      adminHost(s)
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