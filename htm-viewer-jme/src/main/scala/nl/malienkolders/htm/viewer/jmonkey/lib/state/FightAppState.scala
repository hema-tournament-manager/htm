package nl.malienkolders.htm.viewer.jmonkey.lib.state

import com.jme3.font.BitmapText
import nl.malienkolders.htm.viewer.jmonkey.lib.util.TextLabel
import nl.malienkolders.htm.lib.model.MarshalledViewerFight
import nl.malienkolders.htm.lib.model.TotalScore
import nl.malienkolders.htm.lib.HtmHelpers._

abstract class FightAppState extends TournamentAppState("FightRoot") {

  var timeLimit: Long = 2 * 60 * 1000 + 49890
  var timer: Long = 0
  var lastTimerStart: Long = 0
  var running = false
  var stopping = false
  var lastRenderedTime: Long = 0

  var _scores = TotalScore(0, 0, 0, 0, 0, 0, 0, 0)
  def scores = _scores
  def scores_=(s: TotalScore): Unit = {
    _scores = s
    updateLabels
  }

  def timerLabel: BitmapText
  lazy val scoreLabels = List(createScoreLabel, createScoreLabel, createScoreLabel, createScoreLabel, createScoreLabel, createScoreLabel)
  def createScoreLabel: BitmapText

  def info(fight: MarshalledViewerFight) {
    timeLimit = fight.timeLimit
    updateTextLabels(fight)
  }

  def startTimer(time: Long) {
    timer = time
    lastTimerStart = System.currentTimeMillis()
    running = true
  }

  def stopTimer(time: Long) {
    timer = time
    renderTimer
    running = false
  }

  def renderTimer {
    val time = timeLimit - (if (running) (timer + (System.currentTimeMillis() - lastTimerStart)) else timer)

    if (time != lastRenderedTime) {
      timerLabel.setText(renderTime(time))
      lastRenderedTime = time
    }
  }

  override def update(tpf: Float) {
    renderTimer
  }

  def updateLabels {
    scoreLabels(0).setText(_scores.a.toString)
    scoreLabels(1).setText(_scores.aAfter.toString)
    scoreLabels(2).setText(_scores.b.toString)
    scoreLabels(3).setText(_scores.bAfter.toString)
    scoreLabels(4).setText(_scores.double.toString)
    scoreLabels(5).setText(_scores.exchangeCount.toString)
  }

  def updateTextLabels(fight: MarshalledViewerFight)

}