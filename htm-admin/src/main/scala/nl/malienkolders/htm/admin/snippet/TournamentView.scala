package nl.malienkolders.htm.admin.snippet

import net.liftweb._
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.sitemap._
import Loc._
import net.liftweb.util.Helpers._
import net.liftweb.http.js._
import net.liftweb.http.js.JsCmds._
import net.liftweb.mapper._
import nl.malienkolders.htm.lib.rulesets.Ruleset
import nl.malienkolders.htm.lib.model._
import nl.malienkolders.htm.lib.util.Helpers._
import nl.malienkolders.htm.admin.lib._
import nl.malienkolders.htm.admin.lib.exporter._
import nl.malienkolders.htm.admin.lib.Utils.DateTimeRenderHelper
import nl.malienkolders.htm.lib.util.Helpers._
import net.liftweb.http.SHtml.ElemAttr.pairToBasic
import net.liftweb.sitemap.LocPath.stringToLocPath
import net.liftweb.util.IterableConst.itBindable
import net.liftweb.util.IterableConst.itNodeSeqFunc
import net.liftweb.util.IterableConst.itStringPromotable
import net.liftweb.util.StringPromotable.jsCmdToStrPromo
import scala.xml.Text
import scala.xml.EntityRef
import scala.collection.mutable.ListBuffer
import scala.util.Random
import net.liftweb.http.js.JsCmds.{ Reload, RedirectTo }
import scala.xml.Elem

case class ParamInfo(param: String)

object TournamentView {
  val menu = (Menu.param[ParamInfo]("View Tournament", "View Tournament", s => Full(ParamInfo(s)),
    pi => pi.param) / "tournaments" / "view" >> Hidden)
  lazy val loc = menu.toLoc

  def render = {
    val t = {
      val param = TournamentView.loc.currentValue.map(_.param).get
      param match {
        case id if id.matches("\\d+") => Tournament.findByKey(id.toLong).get
        case s => Tournament.find(By(Tournament.identifier, TournamentView.loc.currentValue.map(_.param).get)).get
      }
    }

    val tournamentSubscriptions = t.subscriptions.sortBy(_.experience.is).reverse
    val otherParticipants = Participant.findAll(OrderBy(Participant.name, Ascending)) diff t.subscriptions.map(_.participant.obj.get).toList

    def addParticipant(tournament: Tournament, participantId: Long) = {
      tournament.addParticipant(participantId);
      val participant = Participant.findByKey(participantId).get
      RedirectTo("#participant" + participant.externalId.get) & Reload
    }

    def deleteParticipantFromTournament(tournament: Tournament, participant: Participant) {
      //TODO      tournament.subscriptions -= participant
      tournament.save
      S.notice("%s has been removed from this tournament" format participant.name.is)
    }

    def toggleStar(p: Participant) = {
      p.reload
      if (p.isStarFighter.is)
        p.isStarFighter(false)
      else
        p.isStarFighter(true)
      if (p.save)
        Run("$(\".star" + p.id + "\").attr(\"src\", \"/images/" + (if (p.isStarFighter.get) "star" else "star_gray") + ".png\");")
      else
        Alert("Could not save star status")
    }

    def refresh(anchor: Option[Any] = None) =
      S.redirectTo(t.id.is.toString + anchor.map {
        case p: Participant => "#participant" + p.id.is
        case p: Phase[_] => "#phase" + p.id.is
        case s: String => s
        case _ => ""
      }.getOrElse(""))

    def generateFinals() = {
      val semiFinals = t.eliminationPhase.eliminationFights.takeRight(2)

      // add fights if necessary
      t.finalsPhase.eliminationFights ++= (for (i <- t.finalsPhase.eliminationFights.size to 1) yield EliminationFight.create)

      t.finalsPhase.eliminationFights.head
        .round(1)
        .name("3rd Place")
        .fighterAFuture(Loser(semiFinals(0)).format)
        .fighterBFuture(Loser(semiFinals(1)).format)
      t.finalsPhase.eliminationFights.last
        .round(2)
        .name("1st Place")
        .fighterAFuture(Winner(semiFinals(0)).format)
        .fighterBFuture(Winner(semiFinals(1)).format)
      t.save()
    }

    def generateNextRound(previous: List[EliminationFight], roundNumber: Int): Unit = previous.size match {
      case n if n < 4 => Nil
      case n =>
        val next = (for (i <- 0 to (n / 2 - 1)) yield EliminationFight.create
          .round(roundNumber)
          .name("1/" + (n / 2) + " Finals, Fight " + (i + 1))
          .fighterAFuture(Winner(previous(i * 2)).format)
          .fighterBFuture(Winner(previous(i * 2 + 1)).format)).toList

        t.eliminationPhase.eliminationFights ++= next
        // we have to save the fights to get their id's
        t.save()

        generateNextRound(next, roundNumber + 1)
    }

    def generateElimination(nthFinals: Int) = {
      t.eliminationPhase.eliminationFights.clear()

      val round1 = for (i <- 1 to nthFinals) yield EliminationFight.create
        .round(1)
        .name(s"1/$nthFinals Finals, Fight $i")
        .fighterAFuture(SpecificFighter(None).format)
        .fighterBFuture(SpecificFighter(None).format)

      t.eliminationPhase.eliminationFights ++= round1
      t.save()

      generateNextRound(round1.toList, 2)

      generateFinals()

      RedirectTo("#eliminationphase") & Reload
    }

    def generateEliminationTop2() = {
      t.eliminationPhase.eliminationFights.clear()
      val pools = t.poolPhase.pools
      val poolCount = pools.size
      def generateFromPools(x: Int): (List[EliminationFight], List[EliminationFight]) = x match {
        case _ if x < poolCount / 2 =>
          val (left, right) = generateFromPools(x + 1)
          (EliminationFight.create.round(1).name("1/" + poolCount + " Finals, Fight " + (x + 1)).fighterAFuture(PoolFighter(pools(x), 1).format).fighterBFuture(PoolFighter(pools(poolCount / 2 + x), 2).format) :: left,
            EliminationFight.create.round(1).name("1/" + poolCount + " Finals, Fight " + (poolCount / 2 + x + 1)).fighterAFuture(PoolFighter(pools(poolCount / 2 + x), 1).format).fighterBFuture(PoolFighter(pools(x), 2).format) :: right)
        case _ => (Nil, Nil)
      }
      val (left, right) = generateFromPools(0)
      val round1 = left ++ right

      t.eliminationPhase.eliminationFights ++= round1
      // we have to save the fights to get their id's
      t.save()

      generateNextRound(round1, 2)

      generateFinals()

      RedirectTo("#eliminationphase") & Reload
    }

    def addParticipantToPool(participant: TournamentParticipant, poolId: Long) = {
      if (participant.participant.foreign.get.poolForTournament(t).map(_.id.is).getOrElse(-1) != poolId) {
        t.poolPhase.pools.foreach { pool =>
          pool.participants -= participant.participant.foreign.get
          pool.save
        }

        t.poolPhase.fights.filter(_.inFight_?(participant.participant.foreign.get)).foreach(_.delete_!)

        if (poolId >= 0) {
          val poolTo = t.poolPhase.pools.find(_.id.is == poolId).get
          poolTo.participants += participant.participant.foreign.get
          poolTo.save
        }
        t.save

        GeneratePoolPhase(t).generatePoolFights

        Reload
      } else {
        JsCmds.Noop
      }
    }

    def renderFighter(f: Fight[_, _], side: String, fighter: Fighter) = (fighter match {
      case UnknownFighter(_) =>
        ".label *" #> "?" &
          ".name *" #> <a href={ s"/fights/pick/${f.id.is}${side}" }>Pick a fighter</a> &
          ".club *" #> Nil &
          ".country *" #> Nil
      case knownFighter => knownFighter.participant match {
        case Some(pt) => renderParticipant()(pt.subscription(t).get) & "* [title]" #> knownFighter.toString
        case None =>
          ".label *" #> "?" &
            ".name *" #> knownFighter.toString &
            ".club *" #> Nil &
            ".country *" #> Nil
      }
    }) & ".edit [href]" #> s"/fights/pick/${f.id.is}${side}"

    def renderFights(fights: Seq[Fight[_, _]]) = ".fight" #> fights.map(f =>
      ".fight-title *" #> f.name.get &
        ".scheduled [name]" #> s"fight${f.id.get}" &
        ".edit [href]" #> ("/fights/edit/" + f.phaseType.code + "/" + f.id.get.toString) &
        (f.cancelled.get match {
          case true =>
            ".scheduled [class+]" #> "label-danger" &
              ".scheduled [href]" #> "" &
              ".scheduled *" #> "cancelled"
          case false =>
            f.finished_? match {
              case true =>
                ".scheduled [class+]" #> "label-success" &
                  ".scheduled [href]" #> "" &
                  ".scheduled [title]" #> "Results" &
                  ".scheduled *" #> {
                    val s = f.currentScore
                    s"${s.red} (${s.double}) ${s.blue}"
                  }
              case false =>
                ".scheduled [class+]" #> f.scheduled.foreign.map(_ => "label-info").getOrElse("label-warning") &
                  ".scheduled [href]" #> s"/schedule#fight${f.id.get}" &
                  ".scheduled *" #> f.scheduled.foreign.map(sf => sf.time.get.hhmm).getOrElse("unscheduled")
            }
        })
        &
        ".participant" #> (f.fighterA :: f.fighterB :: Nil).zipWithIndex.map { case (fighter, i) => renderFighter(f, if (i == 0) "A" else "B", fighter) })

    def renderPickPool(sub: TournamentParticipant) = {
      val allOptions = ("-1", "-- No pool --") :: t.poolPhase.pools.map(p => (p.id.is.toString, p.poolName)).toList;
      val currentPoolId = sub.participant.foreign.get.poolForTournament(t).map(_.id.is).getOrElse(-1);

      val enabled = t.poolPhase.fights.filter(f => f.inFight_?(sub.participant.foreign.get) && f.finished_?).size == 0
      SHtml.ajaxSelect(allOptions, Full(currentPoolId.toString),
        s => addParticipantToPool(sub, s.toInt),
        if (!enabled) { "disabled" -> "disabled" } else { "enabled" -> "enabled" })
    }

    import TournamentParticipant._
    implicit class SubscriptionErrorHelper(e: SubscriptionError) {
      def icon: String = e match {
        case _: NotPresent => "user"
        case _: GearNotChecked => "cog"
        case _: HasDroppedOut => "log-out"
        case _ => "flash"
      }
      def clickText: String = e match {
        case _: NotPresent => "Click to mark participant as present"
        case _: GearNotChecked => "Click to mark gear as checked"
        case _: HasDroppedOut => "Click to let the participant re-enter"
        case _ => "Click to set this error as resolved"
      }
    }

    def errors(sub: TournamentParticipant): List[Elem] = sub.errors.map(e =>
      if (e.field.is == e.errorValue) {
        Some(SHtml.a(() => {
          e match {
            case _: HasDroppedOut =>
              t.dropParticipantIn(sub)
            case _ =>
              e.field(!e.errorValue).save()
          }; Reload
        }, <span><span class={ s"glyphicon glyphicon-${e.icon}" }></span> { e.caption }</span>, "title" -> e.clickText))
      } else {
        None
      }).flatten

    def renderParticipant(long: Boolean = false)(sub: TournamentParticipant) = "* [class+]" #> s"participant${sub.participant.foreign.get.externalId.get}" &
      "* [class+]" #> (if (sub.hasError) "danger" else "") &
      ".register [name]" #> s"participant${sub.participant.foreign.get.externalId.get}" &
      ".register [href]" #> s"/participants/register/${sub.participant.obj.get.externalId.get}#tournament${t.id.get}" &
      ".label *" #> sub.fighterNumber.get &
      (if (long) {
        ".name *" #> sub.participant.foreign.get.name.get &
          ".club *" #> sub.participant.foreign.get.club.get &
          ".country *" #> sub.participant.foreign.get.country.foreign.get.name.get
      } else {
        ".name *" #> sub.participant.foreign.get.shortName.get &
          ".club *" #> sub.participant.foreign.get.clubCode.get &
          ".club [title]" #> sub.participant.foreign.get.club.get &
          ".country *" #> sub.participant.foreign.get.country.foreign.get.code2.get &
          ".country [title]" #> sub.participant.foreign.get.country.foreign.get.name.get
      }) &
      ".participant-pick-pool" #> renderPickPool(sub) &
      ".initialRanking input" #> SHtml.ajaxText(sub.experience.get.toString, s => { sub.experience(s.toInt); sub.save; Reload }, "type" -> "number") &
      ".remove" #> (sub.droppedOut.get match {
        case true =>
          SHtml.a(() => { S.notice(s"Dropped ${sub.participant.foreign.get.name.get} back in to this tournament"); t.dropParticipantIn(sub); Reload }, <span class="glyphicon glyphicon-log-in"></span>, "title" -> "Drop in", "data-content" -> "This participant be put back in the tournament. Don't forget to reschedule their fights!")
        case false =>
          sub.hasFought match {
            case true =>
              SHtml.a(() => { S.notice(s"Dropped ${sub.participant.foreign.get.name.get} out of this tournament"); t.dropParticipantOut(sub); Reload }, <span class="glyphicon glyphicon-log-out"></span>, "title" -> "Drop out", "data-content" -> "This participant has already finished some fights and can not be removed from the tournament. All fights of this person will be cancelled.")
            case false =>
              SHtml.a(() => { S.notice(s"Removed ${sub.participant.foreign.get.name.get} from this tournament"); t.removeParticipant(sub); Reload }, <span class="glyphicon glyphicon-remove"></span>, "title" -> "Remove from tournament", "data-content" -> "This participant has not finished any fights and will be removed from the tournament.")
          }
      }) &
      ".error" #> errors(sub)

    var poolCount = 0
    // bindings
    "#tournamentName" #> t.name &
      ".downloadButton" #> Seq(
        "a" #> SHtml.link("/download/pools", () => throw new ResponseShortcutException(downloadPools(t)), Text("Pools")),
        "a" #> SHtml.link("/download/schedule/tournament", () => throw new ResponseShortcutException(downloadSchedule(t)), Text("Schedule"))) &
        "#tournamentParticipantsCount *" #> tournamentSubscriptions.size &
        "#participants" #> (".participant" #> tournamentSubscriptions.map(renderParticipant(true) _)) &
        "#addParticipant" #> (if (otherParticipants.isEmpty) Nil else SHtml.ajaxSelect(("-1", "-- Add Participant --") :: otherParticipants.map(pt => (pt.id.is.toString, pt.name.is)).toList, Full("-1"), id => addParticipant(t, id.toLong), "class" -> "form-control")) &
        (if (t.poolPhase.pools.size.isEven) {
          "#generateElimination-top2" #> SHtml.a(generateEliminationTop2 _, Text("Top 2 per pool"))
        } else {
          "#generateElimination-top2" #> Nil
        }) &
        "#generateElimination-4th" #> SHtml.a(() => generateElimination(4), Text("Quarter Finals")) &
        "#pool-generation-pool-count" #> SHtml.number(t.poolPhase.pools.size, s => poolCount = s, 1, t.subscriptions.size) &
        "#pool-generation-generate" #> SHtml.submit("Generate", () => { GeneratePoolPhase(t).generate(poolCount); S.redirectTo("#poolphase") }) &
        ".tournamentPool" #> t.poolPhase.pools.map(p =>
          ".panel-title" #> (
            ".name *" #> p.poolName &
            "a [href]" #> s"#ranking${p.id.get}") &
            ".participants" #> (".participant" #> p.ranked.map {
              case (pt, _) =>
                renderParticipant()(pt.subscription(t).get)
            }) &
            ".modal" #> (
              "* [id]" #> s"ranking${p.id.get}" &
              ".modal-title *" #> s"Pool ${p.poolName}" &
              "thead" #> (".field" #> t.poolPhase.rulesetImpl.emptyScore.header) &
              ".participant" #> p.ranked.map {
                case (pt, s) =>
                  renderParticipant()(pt.subscription(t).get) &
                    ".field" #> s.row
              })) &
        ".poolPhase" #> t.poolPhase.pools.map(p =>
          renderFights(p.fights)) &
        ".eliminationRound" #> t.eliminationPhase.fights.groupBy(_.round.get).toList.sortBy(_._1).map {
          case (_, fights) => renderFights(fights)
        } &
        ".finals" #> renderFights(t.finalsPhase.fights.reverse) &
        "#phase" #> t.phases.map(p =>
          ".phaseAnchor [name]" #> ("phase" + p.id.get) &
            "#phaseName *" #> <span><a name={ "phase" + p.id.is }></a>{ p.order.is + ": " + p.name.is }</span> &
            "name=ruleset" #> SHtml.ajaxSelect(Ruleset.rulesets.toList.map(r => r._1 -> r._1), Full(p.ruleset.get), { ruleset => p.ruleset(ruleset); p.save; S.notice("Ruleset changed for " + p.name.is) }) &
            "name=timeLimit" #> SHtml.ajaxText((p.timeLimitOfFight.get / 1000).toString, { time => p.timeLimitOfFight(time.toLong seconds); p.save; S.notice("Time limit saved") }, "type" -> "number") &
            "name=fightBreak" #> SHtml.ajaxText((p.breakInFightAt.get / 1000).toString, { time => p.breakInFightAt(time.toLong seconds); p.save; S.notice("Break time saved") }, "type" -> "number") &
            "name=fightBreakDuration" #> SHtml.ajaxText((p.breakDuration.get / 1000).toString, { time => p.breakDuration(time.toLong seconds); p.save; S.notice("Break duration saved") }, "type" -> "number") &
            "name=exchangeLimit" #> SHtml.ajaxText(p.exchangeLimit.toString, { time => p.exchangeLimit(time.toInt); p.save; S.notice("Exchange limit saved") }, "type" -> "number") &
            "name=timeBetweenFights" #> SHtml.ajaxText((p.timeBetweenFights.get / 1000).toString, { time => p.timeBetweenFights(time.toLong seconds); p.save; S.notice("Time between fights saved") }, "type" -> "number")) &
        "#poolphase-ruleset [href]" #> s"/rulesets/modal/${t.poolPhase.ruleset.get}" &
        "#elimination-ruleset [href]" #> s"/rulesets/modal/${t.eliminationPhase.ruleset.get}" &
        "#finals-ruleset [href]" #> s"/rulesets/modal/${t.finalsPhase.ruleset.get}"

  }

  def downloadSchedule(tournament: Tournament) = {
    OutputStreamResponse(ScheduleExporter.doExport(tournament) _, List("content-disposition" -> ("inline; filename=\"schedule_" + tournament.identifier.get + ".xls\"")))
  }

  def downloadPools(tournament: Tournament) = {
    OutputStreamResponse(PoolsExporter.doExport(tournament) _, List("content-disposition" -> ("inline; filename=\"pools_" + tournament.identifier.get + ".xls\"")))
  }

}