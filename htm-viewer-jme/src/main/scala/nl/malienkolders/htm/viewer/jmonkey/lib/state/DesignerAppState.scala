package nl.malienkolders.htm.viewer.jmonkey.lib.state

import com.jme3.input.controls.KeyTrigger
import com.jme3.input.KeyInput
import com.jme3.input.controls.AnalogListener
import nl.malienkolders.htm.viewer.jmonkey.JmeApplication
import com.jme3.font.BitmapText
import com.jme3.math.ColorRGBA
import com.jme3.font.BitmapFont
import com.jme3.input.controls.ActionListener
import spectator.{ FightAppState => SpecFight }
import com.jme3.scene.Node
import nl.malienkolders.htm.lib._
import java.awt.{ Color, Font, GraphicsEnvironment }
import nl.malienkolders.htm.viewer.jmonkey.lib.util._
import nl.malienkolders.htm.lib.model.ViewerMessage
import net.liftweb.util.TimeHelpers._
import nl.malienkolders.htm.lib.model._
import nl.malienkolders.htm.lib.swiss.ParticipantScores

object DesignerAppState extends TournamentAppState("Designer") {

  lazy val outputLabel = createOutputLabel
  def createOutputLabel = {
    val label = new BitmapText(app.getAssetManager.loadFont(
      "Interface/Fonts/Default.fnt"))
    label.setColor(ColorRGBA.Pink)
    label.setText("scores(2): x, y")
    //    label.setAlignment(BitmapFont.Align.Left)
    label
  }

  lazy val message = new TextLabel("", AlignLeft, Raavi, Color.black, (4f, 0.4f), 200, app.getAssetManager())

  def label = controls(control)._2

  lazy val controls = List(
    "scores(0)" -> JmeApplication.fightAppState.scoreLabels(0),
    "scores(1)" -> JmeApplication.fightAppState.scoreLabels(1),
    "scores(2)" -> JmeApplication.fightAppState.scoreLabels(2),
    "scores(3)" -> JmeApplication.fightAppState.scoreLabels(3),
    "scores(4)" -> JmeApplication.fightAppState.scoreLabels(4),
    "scores(5)" -> JmeApplication.fightAppState.scoreLabels(5),
    "timer" -> JmeApplication.fightAppState.timerLabel) ++ (if (JmeApplication.fightAppState == spectator.FightAppState)
      List(
      "phaseName" -> spectator.FightAppState.phaseName,
      "roundName" -> spectator.FightAppState.roundName,
      "fightName" -> spectator.FightAppState.fightName,
      "exchangeLimit" -> spectator.FightAppState.exchangeLimit,
      "fighterBar" -> spectator.FightAppState.fighterBar,
      "name red" -> spectator.FightAppState.fighterRedName,
      "club red" -> spectator.FightAppState.fighterRedClub,
      "name blue" -> spectator.FightAppState.fighterBlueName,
      "club blue" -> spectator.FightAppState.fighterBlueClub,
      "roller" -> spectator.FightAppState.roller)
    else if (JmeApplication.fightAppState == live.FightAppState)
      List(
      "header" -> live.FightAppState.header,
      "barL" -> live.FightAppState.barLeft,
      "barR" -> live.FightAppState.barRight,
      "tournament" -> live.FightAppState.tournamentName)
    else
      List())

  def updateOutput {
    outputLabel.setText("%s: (%f, %f), %f" format (controls(control)._1, label.getLocalTranslation().x, label.getLocalTranslation().y, label.getLocalScale().x))
  }

  var control = 0

  def initializeScene() {
    app.getInputManager().addMapping("left", new KeyTrigger(KeyInput.KEY_LEFT));
    app.getInputManager().addMapping("right", new KeyTrigger(KeyInput.KEY_RIGHT));
    app.getInputManager().addMapping("up", new KeyTrigger(KeyInput.KEY_UP));
    app.getInputManager().addMapping("down", new KeyTrigger(KeyInput.KEY_DOWN));
    app.getInputManager().addMapping("rotUp", new KeyTrigger(KeyInput.KEY_W));
    app.getInputManager().addMapping("rotDown", new KeyTrigger(KeyInput.KEY_S));
    app.getInputManager().addMapping("scaleDown", new KeyTrigger(KeyInput.KEY_A));
    app.getInputManager().addMapping("scaleUp", new KeyTrigger(KeyInput.KEY_D));
    app.getInputManager().addListener(analogListener, "left", "right", "up", "down", "rotDown", "rotUp", "scaleUp", "scaleDown")

    app.getInputManager().addMapping("view1", new KeyTrigger(KeyInput.KEY_1));
    app.getInputManager().addMapping("view2", new KeyTrigger(KeyInput.KEY_2));
    app.getInputManager().addMapping("view3", new KeyTrigger(KeyInput.KEY_3));
    app.getInputManager().addMapping("view4", new KeyTrigger(KeyInput.KEY_4));
    app.getInputManager().addMapping("prev", new KeyTrigger(KeyInput.KEY_SUBTRACT));
    app.getInputManager().addMapping("next", new KeyTrigger(KeyInput.KEY_ADD));
    app.getInputManager().addMapping("fonts", new KeyTrigger(KeyInput.KEY_F1));
    app.getInputManager().addMapping("showmsg", new KeyTrigger(KeyInput.KEY_F2));
    app.getInputManager().addMapping("showpermanentmsg", new KeyTrigger(KeyInput.KEY_F3));
    app.getInputManager().addListener(actionListener, "prev", "next", "view1", "view2", "view3", "view4", "fonts", "showmsg", "showpermanentmsg")

    outputLabel.setLocalTranslation(100, outputLabel.getLineHeight() * 3, 0)
    app.getGuiNode().attachChild(outputLabel)
    updateOutput
  }

  lazy val actionListener = new ActionListener {
    override def onAction(name: String, keyPressed: Boolean, tpf: Float) {
      if (!keyPressed) {
        name match {
          case "prev" => control -= 1
          case "next" => control += 1
          case "view1" =>
            initializeData
            JmeApplication.switch(new EmptyView)
          case "view2" =>
            initializeData
            JmeApplication.switch(new PoolOverview)
          case "view3" =>
            initializeData
            JmeApplication.switch(new PoolRanking)
          case "view4" =>
            initializeData
            JmeApplication.switch(new FightView)
          case "fonts" =>
            GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts().foreach(println _)
          case "showmsg" =>
            val time = scala.util.Random.nextInt(20) + 1
            JmeApplication.showMessage(ViewerMessage("Dit is een test van %d seconden" format time, time seconds))
          case "showpermanentmsg" =>
            JmeApplication.showMessage(ViewerMessage("Deze blijft altijd staan", -1))
        }
        if (control < 0)
          control += controls.size
        control = control % controls.size
        updateOutput
      }
    }
  }

  lazy val analogListener = new AnalogListener {
    override def onAnalog(name: String, value: Float, tpf: Float) {
      val speed = 0.4f
      name match {
        case "left" => label.move(-value * speed, 0f, 0f)
        case "right" => label.move(value * speed, 0f, 0f)
        case "up" => label.move(0f, value * speed, 0f)
        case "down" => label.move(0f, -value * speed, 0f)
        case "rotDown" => label.rotate(value, 0f, 0f)
        case "rotUp" => label.rotate(-value, 0f, 0f)
        case "scaleUp" => label.scale(1 + (value * speed))
        case "scaleDown" => label.scale(1 - (value * speed))

      }
      updateOutput
    }
  }

  val participant1 = MarshalledParticipant(
    1,
    "2",
    "Youval Kuipers",
    "Å, Nyløkken",
    "Stockholmspolisens Idrottsförening Fäktning",
    "SPIFF",
    "NL",
    true, false, true)
  val participant2 = MarshalledParticipant(
    2,
    "58",
    "Friederike von dem Bussche-Hünnefeld",
    "Ł. Dąbrowskič",
    "Espoon Historiallisen Miekkailun Seura",
    "TŠ",
    "BE",
    false, true, false)

  def initializeData {
    JmeApplication.fightAppState.updateTextLabels(MarshalledViewerFight(
      MarshalledTournamentSummary(1, "Open Longsword", "longsword_open", false),
      "1st place",
      1,
      10,
      2 * 60 * 1000,
      participant1,
      participant2))
    JmeApplication.fightAppState.scores = TotalScore(88, 88, 88, 88, 88, 88, 88, 15)

    JmeApplication.poolAppState.updateTextLabels(MarshalledViewerPool(
      MarshalledPoolSummary(1, 1, MarshalledRoundSummary(
        1, 1, "Poule Phase / Round 1", MarshalledTournamentSummary(1, "Longsword Open", "longsword_open", false)), 10, 10),
      List(
        MarshalledViewerFightSummary(
          1,
          participant1,
          participant2,
          true, true,
          TotalScore(1, 2, 3, 4, 5, 0, 0, 8)),
        MarshalledViewerFightSummary(
          2,
          participant2,
          participant1,
          false, false,
          TotalScore(
            1, 2, 3, 4, 5, 0, 0, 8)),
        MarshalledViewerFightSummary(
          3,
          participant2,
          participant1,
          false, false,
          TotalScore(
            1, 2, 3, 4, 5, 0, 0, 8)),
        MarshalledViewerFightSummary(
          4,
          participant2,
          participant1,
          false, false,
          TotalScore(
            1, 2, 3, 4, 5, 0, 0, 8)),
        MarshalledViewerFightSummary(
          5,
          participant2,
          participant1,
          false, false,
          TotalScore(
            1, 2, 3, 4, 5, 0, 0, 8)),
        MarshalledViewerFightSummary(
          6,
          participant2,
          participant1,
          false, false,
          TotalScore(
            1, 2, 3, 4, 5, 0, 0, 8)),
        MarshalledViewerFightSummary(
          7,
          participant2,
          participant1,
          false, false,
          TotalScore(
            1, 2, 3, 4, 5, 0, 0, 8)),
        MarshalledViewerFightSummary(
          8,
          participant2,
          participant1,
          false, false,
          TotalScore(
            1, 2, 3, 4, 5, 0, 0, 8)),
        MarshalledViewerFightSummary(
          9,
          participant2,
          participant1,
          false, false,
          TotalScore(
            1, 2, 3, 4, 5, 0, 0, 8)),
        MarshalledViewerFightSummary(
          10,
          participant2,
          participant1,
          false, false,
          TotalScore(
            1, 2, 3, 4, 5, 0, 0, 8)),
        MarshalledViewerFightSummary(
          11,
          participant2,
          participant1,
          false, false,
          TotalScore(
            1, 2, 3, 4, 5, 0, 0, 8)),
        MarshalledViewerFightSummary(
          12,
          participant2,
          participant1,
          false, false,
          TotalScore(
            1, 2, 3, 4, 5, 0, 0, 8)),
        MarshalledViewerFightSummary(
          13,
          participant2,
          participant1,
          false, false,
          TotalScore(
            1, 2, 3, 4, 5, 0, 0, 8)),
        MarshalledViewerFightSummary(
          14,
          participant2,
          participant1,
          false, false,
          TotalScore(
            1, 2, 3, 4, 5, 0, 0, 8)),
        MarshalledViewerFightSummary(
          15,
          participant2,
          participant1,
          false, false,
          TotalScore(
            1, 2, 3, 4, 5, 0, 0, 8)),
        MarshalledViewerFightSummary(
          16,
          participant2,
          participant1,
          false, false,
          TotalScore(
            1, 2, 3, 4, 5, 0, 0, 8)))))

    JmeApplication.rankingAppState.updateTextLabels(MarshalledPoolRanking(
      MarshalledPoolSummary(1, 1, MarshalledRoundSummary(
        1, 1, "Round 1", MarshalledTournamentSummary(1, "Longsword Open", "longsword_open", false)), 10, 10),
      List(
        participant1,
        participant2,
        participant1,
        participant1,
        participant2,
        participant1,
        participant1,
        participant2,
        participant1,
        participant2,
        participant1,
        participant2,
        participant1,
        participant1,
        participant2,
        participant1,
        participant2,
        participant1,
        participant1,
        participant2,
        participant1,
        participant1,
        participant2,
        participant2),
      List(
        ParticipantScores(1, 1, 0, 2, 1, 1, 1, 1, 1, 1, 1, 1),
        ParticipantScores(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        ParticipantScores(1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        ParticipantScores(1, 1, 0, 2, 1, 1, 1, 1, 1, 1, 1, 1),
        ParticipantScores(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        ParticipantScores(1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        ParticipantScores(1, 1, 0, 2, 1, 1, 1, 1, 1, 1, 1, 1),
        ParticipantScores(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        ParticipantScores(1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        ParticipantScores(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        ParticipantScores(1, 1, 0, 2, 1, 1, 1, 1, 1, 1, 1, 1),
        ParticipantScores(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        ParticipantScores(1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        ParticipantScores(1, 1, 0, 2, 1, 1, 1, 1, 1, 1, 1, 1),
        ParticipantScores(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        ParticipantScores(1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        ParticipantScores(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        ParticipantScores(1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        ParticipantScores(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        ParticipantScores(1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        ParticipantScores(1, 1, 0, 2, 1, 1, 1, 1, 1, 1, 1, 1),
        ParticipantScores(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        ParticipantScores(1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        ParticipantScores(1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1))))
  }

}