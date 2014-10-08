package nl.malienkolders.htm.admin.snippet

import net.liftweb._
import http._
import mapper._
import util.Helpers._
import scala.xml.NodeSeq
import nl.malienkolders.htm.lib.model._

class TournamentList {

  def render = {
    var name = ""
    var identifier = ""
    var label = ""

    def process(): Unit = {
      if (Arena.count < 1) {
        Arena.create.name("Arena 1").save()
      }

      val t = Tournament.create.identifier(identifier).name(name).mnemonic(label)
      t.save()

      S.redirectTo("/tournaments/list")
    }

    "#newTournament" #> (
      "#name" #> SHtml.text(name, name = _) &
      "#identifier" #> SHtml.text(identifier, identifier = _) &
      "#label" #> SHtml.text(label, label = _) &
      "#submitNewTournament" #> SHtml.submit("Submit", process))
  }

}