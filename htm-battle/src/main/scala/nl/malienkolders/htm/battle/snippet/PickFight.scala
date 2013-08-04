package nl.malienkolders.htm.battle
package snippet

import comet._
import nl.malienkolders.htm.lib.model._

import net.liftweb._
import common._
import util._
import http._
import Helpers._
import dispatch._
import Http._
import json._

class PickFight {

  implicit val formats = Serialization.formats(NoTypeHints)

  def render = {
    def peek(p: MarshalledPoolSummary) = {
      val req = :/(ShowAdminConnection.adminHost) / "api" / "pool" / p.id.toString / "fight" / "peek"
      Http(req OK as.String).fold[Box[MarshalledFight]](
        _ => Empty,
        success => success match {
          case "false" => Empty
          case _ => Full(Serialization.read[MarshalledFight](success))
        }).apply
    }

    def pickPool(p: MarshalledPoolSummary) = {
      BattleServer !! SetCurrentFight(p)
      S.redirectTo("/fight/")
    }

    val reqPools = BattleServer !! RequestCurrentPool
    reqPools match {
      case Full(p: MarshalledPoolSummary) =>
        ".fightChoice" #> (for {
          f <- peek(p)
        } yield {
          "name=tournamentName" #> p.round.tournament.name &
            "name=roundName" #> p.round.name &
            "name=poolOrder" #> p.order.toString &
            "name=fighterRed" #> <span class="red">{ f.fighterA.name }</span> &
            "name=fighterBlue" #> <span class="blue">{ f.fighterB.name }</span> &
            "* [onclick]" #> SHtml.ajaxInvoke(() => pickPool(p))
        })
      case x => {
        println(x)
        "* *" #> "No fights!"
      }
    }
  }

}