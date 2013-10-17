package nl.malienkolders.htm.admin.comet

import _root_.net.liftweb._
import http._
import common._
import actor._
import util._
import mapper._
import Helpers._
import _root_.scala.xml.{ NodeSeq, Text }
import _root_.java.util.Date
import nl.malienkolders.htm.lib.model._

object FightServer extends LiftActor {

  def messageHandler = {
    case PeekFight(p) => {
      reply(p.nextFight.map(FightMsg(_)).getOrElse(NoFightMsg))
    }
    case PopFight(p) => {
      reply(
        p.nextFight.map { firstFight =>
          firstFight.inProgress(true)
          firstFight.save
          FightMsg(firstFight)
        }.getOrElse(NoFightMsg))
    }
    case FightMsg(f) => {
      f.inProgress(false)
      reply(if (f.save) FightMsg(f) else NoFightMsg)
    }
    case FightResult(f, c) => {
      var fight = f
      if (!c)
        fight = Fight.findByKey(f.id.is).get

      fight.inProgress(false)
      reply(if (fight.save) FightMsg(fight) else NoFightMsg)
    }
    case FightUpdate(f) => {
      val fight = Fight.findByKey(f.id).get.fromMarshalledSummary(f)
      fight.inProgress(f.timeStop == 0).save
      val arena = fight.pool.obj.get.arena.obj.get
      arena.viewers.foreach { viewer =>
        viewer.rest.fightUpdate(arena, fight)
      }
    }

    case TimerUpdate(f, TimerMessage(action, time)) => {
      val fight = Fight.findByKey(f).get
      val arena = fight.pool.obj.get.arena.obj.get
      arena.viewers.foreach { viewer =>
        viewer.rest.timerUpdate(arena, action, time)
      }
    }
  }

}

case class TimerMessage(action: String, time: Long)
case class TimerUpdate(fightId: Long, msg: TimerMessage)
case class PeekFight(pool: Pool)
case class PopFight(pool: Pool)
case class FightMsg(fight: Fight)
case class FightResult(fight: Fight, confirm: Boolean)
case class FightUpdate(fight: MarshalledFightSummary)
case object NoFightMsg