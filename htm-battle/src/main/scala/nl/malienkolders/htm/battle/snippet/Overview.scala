package nl.malienkolders.htm.battle
package snippet

import net.liftweb._
import common._
import http._
import util.Helpers._
import json._
import nl.malienkolders.htm.lib.model._
import dispatch._
import Http._
import comet._
import net.liftweb.common.Box
import scala.xml.Text
import js._
import JsCmds._
import scala.concurrent._
import ExecutionContext.Implicits.global

object Overview {

  implicit val formats = Serialization.formats(NoTypeHints)

  def render = {

    def fetchRound(roundId: Long) = {
      val req = :/(ShowAdminConnection.adminHost) / "api" / "round" / roundId.toString
      Http(req OK as.String).fold[MarshalledRound](
        _ => MarshalledRound(-1, -1, "Round not found", 0, 0, 0, 0, 0, List()),
        success => Serialization.read[MarshalledRound](success)).apply
    }

    def subscribePool(pool: MarshalledPoolSummary) = {
      val subscription = BattleServer !! PoolSubscription(pool)
      println("subscription: " + subscription)
      val subscribed = subscription match {
        case Full(true) => true
        case m =>
          println("subscribed: " + m)
          false
      }

      val message = if (subscribed) UnsubscribePool(pool) else SubscribePool(pool)
      BattleServer ! message
      var jsAction = "$('.subscribed').removeClass('subscribed').addClass('unsubscribed');" +
        "$('.pool span').text('Subscribe');";
      if (!subscribed)
        jsAction += "$('#pool" + pool.id + "').removeClass('unsubscribed').addClass('subscribed');" +
          "$('#pool" + pool.id + " span').text('Unsubscribe');"
      Run(jsAction)

    }

    val ts = {
      val req = :/(ShowAdminConnection.adminHost) / "api" / "tournaments"
      Http(req OK as.String).fold[List[MarshalledTournament]](
        _ => List(),
        success => Serialization.read[List[MarshalledTournament]](success)).apply
    }
    ".tournament" #> ts.map(t =>
      "h2 *" #> t.name &
        ".round" #> t.rounds.map { rId =>
          val r = fetchRound(rId)
          "h3 *" #> r.name &
            ".pool" #> r.pools.map { p =>
              val status = BattleServer !! PoolSubscription(p)
              "* [id]" #> ("pool" + p.id) &
                "* [class]" #> {
                  status.map {
                    case true => "subscribed"
                    case false => "unsubscribed"
                  } openOr "unknown"
                } &
                "span" #> p.order.toString &
                "button" #> SHtml.ajaxButton(status.map { case true => "Unsubscribe"; case false => "Subscribe" } openOr "?", () => subscribePool(p))
            }
        })
  }

}