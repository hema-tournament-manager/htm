package nl.malienkolders.htm.admin
package snippet

import net.liftweb._
import http._
import js._
import JsCmds._
import JE._

import comet.ChatServer

object ChatIn {
  def render = SHtml.onSubmit(s => {
    ChatServer ! s
    SetValById("chat_in", "")
  })
}