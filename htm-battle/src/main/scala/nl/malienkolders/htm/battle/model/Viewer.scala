package nl.malienkolders.htm.battle
package model

import net.liftweb._
import mapper._
import common._
import util._
import Helpers._
import scala.xml._
import comet.ViewerServer
import dispatch._
import Http._
import net.liftweb.json._
import lib._
import nl.malienkolders.htm.lib.model.{ MarshalledRound, MarshalledPoolSummary, MarshalledFight, MarshalledViewerFight, TotalScore, MarshalledViewerPool }
import nl.malienkolders.htm.lib._
import EncodingHelpers._
import snippet.ShowAdminConnection
import nl.malienkolders.htm.lib.model.ViewerMessage
import nl.malienkolders.htm.lib.model.MarshalledPoolRanking

class Viewer extends LongKeyedMapper[Viewer] with IdPK with CreatedUpdated {
  def getSingleton = Viewer

  object alias extends MappedPoliteString(this, 32)
  object hostname extends MappedString(this, 64)
  object port extends MappedInt(this)
  object screen extends MappedInt(this)
  object mode extends MappedString(this, 32)

  object rest {
    def baseRequest = :/("%s:%d" format (hostname.get, port.get))
    implicit val formats = Serialization.formats(ShortTypeHints(List(classOf[EmptyView], classOf[PoolOverview], classOf[PoolRanking], classOf[FightView])))

    def ping = {
      val req = baseRequest / "ping"
      Http(req OK as.String).fold(
        _ => false,
        success => success.contains("pong")).apply
    }

    def poll = {
      val req = baseRequest / "poll"
      Http(req OK as.String).fold(
        _ => List[Screen](),
        success => parse(success).extract[List[Screen]]).apply
    }

    def state = {
      val req = baseRequest / "state"
      Http(req OK as.String).fold(
        _ => false,
        success => asBoolean(success) match {
          case Full(b) => b
          case _ => false
        }).apply
    }

    def boot = {
      val req = baseRequest / "boot" / mode.get / screen.get.toString
      Http(req OK as.String).fold(
        _ => true,
        success => asBoolean(success) match {
          case Full(b) => b
          case _ => false
        }).apply
    }

    def shutdown = {
      val req = baseRequest / "shutdown"
      Http(req OK as.String).fold(
        _ => true,
        success => asBoolean(success) match {
          case Full(b) => b
          case _ => false
        }).apply
    }

    def changeScreen(screenIndex: Int) = {
      val req = baseRequest / "change" / screenIndex.toString
      Http(req OK as.String).fold(
        _ => true,
        success => asBoolean(success) match {
          case Full(b) => b
          case _ => false
        }).apply
    }

    def switch(view: View) = {
      val req = baseRequest / "switch" <:< Map("Content-Type" -> "application/json")
      val json = Serialization.write(view)
      val result = Http(req.POST << json).fold[Boolean](_ => false, resp => resp.getResponseBody().toBoolean).apply
      result
    }

    def message(text: String, duration: Long) = {
      val json = Serialization.write(ViewerMessage(text, duration))
      val req = baseRequest / "message"
      val result = Http(req.POST << encodeBase64(json) OK as.String).fold[Boolean](_ => false, resp => resp.toBoolean).apply
      result
    }

    object pool {
      def init(pool: MarshalledViewerPool) = {
        val json = Serialization.write(pool)
        val req = baseRequest / "init" / "pool"
        val result = Http(req.POST << encodeBase64(json) OK as.String).fold[Boolean](_ => false, resp => resp.toBoolean).apply
        result
      }

      def ranking(pool: MarshalledPoolRanking) = {
        val json = Serialization.write(pool)
        val req = baseRequest / "init" / "ranking"
        val result = Http(req.POST << encodeBase64(json) OK as.String).fold[Boolean](_ => false, resp => resp.toBoolean).apply
        result
      }
    }

    object fight {
      def init(round: MarshalledRound, pool: MarshalledPoolSummary, fight: MarshalledFight) = {
        val fightSummary = MarshalledViewerFight(
          pool.round.tournament,
          round.name,
          fight.order,
          round.exchangeLimit,
          round.timeLimitOfFight,
          fight.fighterA,
          fight.fighterB)
        val json = Serialization.write(fightSummary)
        val req = baseRequest / "init" / "fight"
        val result = Http(req.POST << encodeBase64(json) OK as.String).fold[Boolean](_ => false, resp => resp.toBoolean).apply
        result
      }

      def score(scores: TotalScore) = {
        val json = Serialization.write(scores)
        val req = baseRequest / "score" <:< Map("Content-Type" -> "application/json")
        Http(req.POST << json).fold(
          _ => true,
          success => asBoolean(success.getResponseBody()) match {
            case Full(b) => b
            case _ => false
          }).apply
      }

    }

    object timer {
      def start(time: Long) = {
        val req = baseRequest / "timer" / "start" / time.toString
        Http(req OK as.String).fold(
          _ => true,
          success => asBoolean(success) match {
            case Full(b) => b
            case _ => false
          }).apply
      }
      def stop(time: Long) = {
        val req = baseRequest / "timer" / "stop" / time.toString
        Http(req OK as.String).fold(
          _ => true,
          success => asBoolean(success) match {
            case Full(b) => b
            case _ => false
          }).apply
      }
    }
  }
}

object Viewer extends Viewer with LongKeyedMetaMapper[Viewer] {
  override def dbTableName = "viewers"
}