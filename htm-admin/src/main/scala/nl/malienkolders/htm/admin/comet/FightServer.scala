package nl.malienkolders.htm.admin.comet

import _root_.net.liftweb._
import http._
import common._
import actor._
import util._
import mapper._
import Helpers._
import json._
import JsonDSL._
import _root_.scala.xml.{ NodeSeq, Text }
import _root_.java.util.Date
import nl.malienkolders.htm.lib.model._
import scala.util.Either

object FightServer extends LiftActor {

  def messageHandler = {
    case PeekFight(p) => {
      reply(p.nextFight.map(f => FightMsg(f)).getOrElse(NoFightMsg))
    }
    case PopFight(p) => {
      reply(
        p.nextFight.map { firstFight =>
          firstFight.inProgress(true)
          firstFight.save
          FightMsg(firstFight)
        }.getOrElse(NoFightMsg))
    }
    case FightMsg(f) =>
      f.inProgress(false)
      reply(if (f.save) FightMsg(f) else NoFightMsg)

    case FightResult(f, c) => {
      var fight: Fight[_, _] = f
      if (!c)
        fight = FightHelper.dao(fight.phaseType).findByKey(fight.id.is).get

      fight.inProgress(false)
      reply(if (fight.save) FightMsg(fight) else NoFightMsg)
    }
    case FightUpdate(f) => {
      val fight: Fight[_, _] = FightHelper.dao(f.phaseType).findByKey(f.id).asInstanceOf[Box[Fight[_, _]]].get.fromMarshalledSummary(f).asInstanceOf[Fight[_, _]]
      fight.inProgress(f.timeStop == 0).asInstanceOf[Fight[_, _]].save
      val arena = fight.scheduled.foreign.get.timeslot.foreign.get.arena.foreign.get
      arena.viewers.foreach { viewer =>
        viewer.rest.fightUpdate(arena, fight)
      }
    }

    case PostponeFight(f) => {
      val fight: Fight[_, _] = FightHelper.dao(f.phaseType).findByKey(f.id).asInstanceOf[Box[Fight[_, _]]].get.fromMarshalledSummary(f).asInstanceOf[Fight[_, _]]
      val scheduled = fight.scheduled.foreign.get
      scheduled.delete_!
      fight.scheduled(Empty)
      fight.save()
    }

    case TimerUpdate(f, TimerMessage(action, time)) => {
      val fight = FightHelper.dao(f.phase).findByKey(f.id).get
      val arena = fight.scheduled.foreign.get.timeslot.foreign.get.arena.foreign.get
      arena.viewers.foreach { viewer =>
        viewer.rest.timerUpdate(arena, action, time)
      }
    }

    case MessageUpdate(f, message) => {
      val fight = FightHelper.dao(f.phase).findByKey(f.id).get
      val arena = fight.scheduled.foreign.get.timeslot.foreign.get.arena.foreign.get
      arena.viewers.foreach { viewer =>
        viewer.rest.fightUpdate(arena, "message" -> message)
      }
    }
  }

}

case class MessageUpdate(fightId: FightId, msg: String)
case class TimerMessage(action: String, time: Long)
case class TimerUpdate(fightId: FightId, msg: TimerMessage)
case class PeekFight(pool: Pool)
case class PopFight(pool: Pool)
case class FightMsg[F <: Fight[_, _]](fight: F)
case class FightResult[F <: Fight[_, _]](fight: F, confirm: Boolean)
case class FightUpdate(fight: MarshalledFight)
case class PostponeFight(fight: MarshalledFight)
case object NoFightMsg