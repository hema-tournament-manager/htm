package nl.malienkolders.htm.lib
package model

import net.liftweb._
import mapper._
import common._
import util._
import Helpers._
import scala.xml._
import dispatch._
import Http._
import net.liftweb.json._
import scala.concurrent._
import ExecutionContext.Implicits.global

case class ViewerMessage(message: String, duration: Long)

case class Screen(id: String, width: Int, height: Int, fullscreenSupported: Boolean)

class Viewer extends LongKeyedMapper[Viewer] with IdPK with CreatedUpdated {
  def getSingleton = Viewer

  object alias extends MappedPoliteString(this, 32)
  object hostname extends MappedString(this, 64)
  object port extends MappedInt(this)
  object screen extends MappedInt(this)

  object rest {
    def baseRequest = :/("%s:%d" format (hostname.get, port.get))
    implicit val formats = Serialization.formats(NoTypeHints)

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
      val req = baseRequest / "boot" / screen.get.toString
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

    def score(scores: (Int, Int, Int)) = {
      val req = baseRequest / "score" / scores._1.toString / scores._2.toString / scores._3.toString
      Http(req OK as.String).fold(
        _ => true,
        success => asBoolean(success) match {
          case Full(b) => b
          case _ => false
        }).apply
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

object Viewer extends Viewer with LongKeyedMetaMapper[Viewer] with CRUDify[Long, Viewer] {
  override def dbTableName = "viewers"
}