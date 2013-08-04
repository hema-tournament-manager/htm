package nl.malienkolders.htm.battle
package snippet

import net.liftweb._
import http._
import util.Helpers._
import nl.malienkolders.htm.battle.model.Viewer
import net.liftweb.util.ClearClearable

object Viewers {
  def render = {

    "li *" #> Viewer.findAll.map(v => v.alias.get) & ClearClearable
  }
}