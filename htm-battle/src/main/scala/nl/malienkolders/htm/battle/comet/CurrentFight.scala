package nl.malienkolders.htm.battle
package comet

import model._
import net.liftweb._
import common._
import http._
import util._
import Helpers._
import js._
import JsCmds._
import scala.xml.Text
import nl.malienkolders.htm.lib.model._
import scala.xml.Elem
import nl.malienkolders.htm.lib.HtmHelpers._

class CurrentFight extends CometActor with CometListener {

  override def defaultPrefix = Full("comet")
  def registerWith = BattleServer

  var currentFight: Box[MarshalledFight] = Empty
  var currentPool: MarshalledPoolSummary = _
  var currentRound: MarshalledRound = _
  var scores: List[Score] = List()

  var currentTime: Long = 0

  def currentScore = try {
    scores.foldLeft((0, 0, 0, 0, 0)) { case ((a, aa, b, ba, d), s) => (a + s.a, aa + s.aAfter, b + s.b, ba + s.bAfter, d + s.double) }
  } catch {
    case _: Throwable => (0, 0, 0, 0, 0)
  }

  def addScore(sp: ScorePoints) = {
    BattleServer ! Stop
    BattleServer ! Score(sp.a, sp.aAfter, sp.b, sp.bAfter, sp.double, new java.util.Date().getTime(), currentTime, sp.remark, sp.isSpecial, sp.isExchange)
  }

  def render = {
    val r = currentRound
    val p = currentPool
    val cs = currentScore

    def renderRuleTime(time: Long) =
      if (time % 60000 == 0) {
        (time / 60000) + " minutes"
      } else {
        (time / 1000) + " seconds"
      }

    currentFight.map { f =>
      "name=start [onclick]" #> SHtml.ajaxInvoke(() => {
        BattleServer ! Start
        Noop
      }) &
        "name=stop [onclick]" #> SHtml.ajaxInvoke(() => {
          BattleServer ! Stop
          Noop
        }) &
        "name=tournamentName" #> p.round.tournament.name &
        "name=roundName" #> r.name &
        "name=poolOrder" #> p.order.toString &
        "name=timeLimit" #> (renderRuleTime(r.timeLimitOfFight)) &
        "name=timeBreak" #> (if (r.breakInFightAt == 0) "no break" else ("break at " + renderRuleTime(r.breakInFightAt))) &
        "name=exchangeLimit" #> (if (r.exchangeLimit == 0) "no exchange limit" else ("limited to " + r.exchangeLimit + " exchanges")) &
        "#timer *" #> renderTime(currentRound.timeLimitOfFight - currentTime) &
        "#fighters" #> (
          ".names" #> (".red *" #> f.fighterA.name &
            ".blue *" #> f.fighterB.name) &

            ".clubs" #> (".red *" #> f.fighterA.club &
              ".blue *" #> f.fighterB.club)) &
              "name=scoreAPlus [onclick]" #> SHtml.ajaxInvoke(() => addScore(ScorePoints(1, 0, 0, 0, 0, "Correction: Score Red +1", false, false))) &
              "name=scoreAMin [onclick]" #> SHtml.ajaxInvoke(() => addScore(ScorePoints(-1, 0, 0, 0, 0, "Correction: Score Red -1", false, false))) &
              "name=scoreAAfterPlus [onclick]" #> SHtml.ajaxInvoke(() => addScore(ScorePoints(0, 1, 0, 0, 0, "Correction: Afterblows Red +1", false, false))) &
              "name=scoreAAfterMin [onclick]" #> SHtml.ajaxInvoke(() => addScore(ScorePoints(0, -1, 0, 0, 0, "Correction: Afterblows Red -1", false, false))) &
              "name=scoreBPlus [onclick]" #> SHtml.ajaxInvoke(() => addScore(ScorePoints(0, 0, 1, 0, 0, "Correction: Score Blue +1", false, false))) &
              "name=scoreBMin [onclick]" #> SHtml.ajaxInvoke(() => addScore(ScorePoints(0, 0, -1, 0, 0, "Correction: Score Blue -1", false, false))) &
              "name=scoreBAfterPlus [onclick]" #> SHtml.ajaxInvoke(() => addScore(ScorePoints(0, 0, 0, 1, 0, "Correction: Afterblows Blue +1", false, false))) &
              "name=scoreBAfterMin [onclick]" #> SHtml.ajaxInvoke(() => addScore(ScorePoints(0, 0, 0, -1, 0, "Correction: Afterblows Blue -1", false, false))) &
              "name=scoreDPlus [onclick]" #> SHtml.ajaxInvoke(() => addScore(ScorePoints(0, 0, 0, 0, 1, "Correction: Double Hits +1", false, false))) &
              "name=scoreDMin [onclick]" #> SHtml.ajaxInvoke(() => addScore(ScorePoints(0, 0, 0, 0, -1, "Correction: Double Hits -1", false, false))) &
              (if (p.round.tournament.rapier) {
                "#scoringButtons [class]" #> "invisible" &
                  "#scoringButtonsRapier" #> (
                    "name=2hitA [onclick]" #> SHtml.ajaxInvoke(() => addScore(ScorePoints(2, 0, 0, 0, 0, "2 Points for Red", true, true))) &
                    "name=2hitB [onclick]" #> SHtml.ajaxInvoke(() => addScore(ScorePoints(0, 0, 2, 0, 0, "2 Points for Blue", true, true))) &
                    "name=1hitA [onclick]" #> SHtml.ajaxInvoke(() => addScore(ScorePoints(1, 0, 0, 0, 0, "1 Point for Red", false, true))) &
                    "name=1hitB [onclick]" #> SHtml.ajaxInvoke(() => addScore(ScorePoints(0, 0, 1, 0, 0, "1 Point for Blue", false, true))) &
                    "name=2hitA_1hitB [onclick]" #> SHtml.ajaxInvoke { () =>
                      addScore(ScorePoints(1, 0, 0, 0, 0, "2 Points for Red and 1 Point for Blue", true, true))
                    } &
                    "name=2hitB_1hitA [onclick]" #> SHtml.ajaxInvoke { () =>
                      addScore(ScorePoints(0, 0, 1, 0, 0, "2 Points for Blue and 1 Point for Red", true, true))
                    } &
                    "name=2double [onclick]" #> SHtml.ajaxInvoke(() => addScore(ScorePoints(0, 0, 0, 0, 2, "2 Point Double Hit", false, true))) &
                    "name=1double [onclick]" #> SHtml.ajaxInvoke(() => addScore(ScorePoints(0, 0, 0, 0, 1, "1 Point Double Hit", false, true))) &
                    "#undoR [onclick]" #> SHtml.ajaxInvoke { () =>
                      BattleServer ! Undo
                    })

              } else {
                "#scoringButtonsRapier [class]" #> "invisible" &
                  "#scoringButtons" #> (
                    "name=hitA [onclick]" #> SHtml.ajaxInvoke(() => addScore(ScorePoints(1, 0, 0, 0, 0, "Point for Red", false, true))) &
                    "name=hitB [onclick]" #> SHtml.ajaxInvoke(() => addScore(ScorePoints(0, 0, 1, 0, 0, "Point for Blue", false, true))) &
                    "name=afterAB [onclick]" #> SHtml.ajaxInvoke(() => addScore(ScorePoints(0, 1, 0, 0, 0, "Afterblow on Red", false, true))) &
                    "name=afterBA [onclick]" #> SHtml.ajaxInvoke(() => addScore(ScorePoints(0, 0, 0, 1, 0, "Afterblow on Blue", false, true))) &
                    "name=double [onclick]" #> SHtml.ajaxInvoke(() => addScore(ScorePoints(0, 0, 0, 0, 1, "Double Hit", false, true))) &
                    "#undo [onclick]" #> SHtml.ajaxInvoke { () => BattleServer ! Undo })
              }) &
              "#scoreA *" #> cs._1 &
              "#scoreAAfter *" #> cs._2 &
              "#doubleHits *" #> cs._5 &
              "#scoreB *" #> cs._3 &
              "#scoreBAfter *" #> cs._4 &
              "name=finish [onclick]" #> SHtml.ajaxInvoke(() => BattleServer ! Finish)
    }.getOrElse[CssSel]("*" #> "There is no active Fight. Use the \"Next Fight\" button or subscribe to a Poule.")

  }

  def updateScores(newScores: List[Score]) = {
    implicit def intToText(i: Int): Text = Text(i.toString())
    val oldScores = scores
    scores = newScores
    val cs = currentScore
    println("updating scores")
    partialUpdate(
      SetHtml("scoreA", cs._1) &
        SetHtml("scoreAAfter", cs._2) &
        SetHtml("scoreB", cs._3) &
        SetHtml("scoreBAfter", cs._4) &
        SetHtml("doubleHits", cs._5) &
        (if (scores.size > oldScores.size)
          scores.reverse.drop(oldScores.size + 1).map(s => Run("$('#scoreHistory tbody').append('<tr><td>x</td><td></td><td>" + s.remark + "</td></tr>');"))
        else
          Run("$('#scoreHistory tbody tr:gt(" + (scores.size - 1) + ")').remove();")))
  }

  def updateTimer(newTime: Long) = {
    currentTime = newTime
    partialUpdate(SetHtml("timer", Text(renderTime(currentRound.timeLimitOfFight - newTime))))
  }

  override def lowPriority = {
    case BattleServerUpdate(r, p, f, _, t, ss) => {
      if (r.isDefined)
        currentRound = r.get
      if (p.isDefined)
        currentPool = p.get
      val fightChanged = f != currentFight
      currentFight = f
      currentTime = t
      scores = ss
      if (fightChanged) {
        reRender(true)
        partialUpdate(Run("$('button').button();"))
      } else {
        updateScores(ss)
        updateTimer(t)
      }
    }
    case ScoreUpdate(ss) =>
      updateScores(ss)
    case TimerUpdate(t) =>
      updateTimer(t)
  }
  //  def updateScore = {
  //   val cs = fight.get.currentScore 
  //  SetHtml("scoreA", Text(cs._1.toString())) &
  //    SetHtml("scoreB", Text(cs._2.toString())) &
  //    SetHtml("doubleHits", Text(cs._3.toString()))
  //  }

}