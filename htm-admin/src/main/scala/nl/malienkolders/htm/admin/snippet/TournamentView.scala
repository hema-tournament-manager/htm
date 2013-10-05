package nl.malienkolders.htm.admin.snippet

import net.liftweb._
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.sitemap._
import net.liftweb.util.Helpers._
import net.liftweb.http.js._
import net.liftweb.http.js.JsCmds._
import net.liftweb.mapper._
import nl.malienkolders.htm.lib.model._
import nl.malienkolders.htm.lib.HtmHelpers._
import nl.malienkolders.htm.admin.lib._
import nl.malienkolders.htm.admin.lib.TournamentUtils._
import net.liftweb.http.SHtml.ElemAttr.pairToBasic
import net.liftweb.sitemap.LocPath.stringToLocPath
import net.liftweb.util.IterableConst.itBindable
import net.liftweb.util.IterableConst.itNodeSeqFunc
import net.liftweb.util.IterableConst.itStringPromotable
import net.liftweb.util.StringPromotable.jsCmdToStrPromo
import scala.xml.Text
import scala.xml.EntityRef
import scala.xml.EntityRef
import scala.collection.mutable.ListBuffer
import scala.util.Random
import nl.malienkolders.htm.lib.SwissTournament

case class ParamInfo(param: String)

object TournamentView {
  val menu = Menu.param[ParamInfo]("View Tournament", "View Tournament", s => Full(ParamInfo(s)),
    pi => pi.param) / "tournaments" / "view"
  lazy val loc = menu.toLoc

  def render = {
    val t = {
      val param = TournamentView.loc.currentValue.map(_.param).get
      param match {
        case id if id.matches("\\d+") => Tournament.findByKey(id.toLong).get
        case s => Tournament.find(By(Tournament.identifier, TournamentView.loc.currentValue.map(_.param).get)).get
      }
    }

    val tournamentParticipants = t.participants.sortBy(_.name.is)
    val otherParticipants = Participant.findAll(OrderBy(Participant.name, Ascending)) diff t.participants.toList

    var fighterA: Box[Participant] = Empty
    var fighterB: Box[Participant] = Empty

    def finishedFights_?(round: Round) = {
      round.pools.exists(_.fights.exists(_.finished_?))
    }

    def addParticipant(tournament: Tournament, participantId: Long) {
      val participant = Participant.findByKey(participantId).get
      tournament.participants += participant
      tournament.save
      refresh()
    }
    def newRound(name: String) {
      val round = Round.create.name(name).order(t.rounds.size + 1).timeLimitOfFight(120 seconds).breakInFightAt(0 seconds).exchangeLimit(10)
      t.rounds += round
      t.save
      val pool = Pool.create.order(1)
      round.pools += pool
      round.save
      if (round.order == 1) {
        pool.participants ++= t.participants.filter(pt => pt.isPresent.is && pt.isEquipmentChecked.is)
        pool.save
      }
    }
    def deleteRound(tournament: Tournament, round: Round) {
      tournament.rounds -= round
      tournament.rounds.toList.zipWithIndex.foreach { case (r, i) => r.order(i + 1) }
      tournament.save
    }
    def redistributePools(round: Round, extraParticipants: List[Participant]) {
      def snake(poolCount: Int, participantCount: Int) =
        (1 to participantCount).foldLeft((true, List[Int](0))) {
          case ((up, s), i) =>
            if (up && s.head == poolCount - 1) {
              (!up, poolCount - 1 :: s)
            } else if (!up && s.head == 0) {
              (!up, 0 :: s)
            } else if (up) {
              (up, (s.head + 1) :: s)
            } else {
              (up, (s.head - 1) :: s)
            }
        }._2.reverse

      if (finishedFights_?(round)) {
        S.notice("Cannot shuffle pools, because fights have been fought")
      } else {
        val ranked = (round.pools.flatMap(_.participants).toList ++ extraParticipants).sortBy(_ => Random.nextDouble()).sortBy(_.initialRanking).reverse
        val pools = round.pools.map(_ => ListBuffer[Participant]())

        ranked.zip(snake(pools.size, ranked.size)).foreach {
          case (p, i) => pools(i) += p
        }

        while (pools.count(_.size.odd_?) > 1) {
          val takeFrom = pools.reverse.find(_.size.odd_?).head
          val p = takeFrom.last
          takeFrom -= p
          val addTo = pools.find(_.size.odd_?).head
          addTo += p
        }

        round.pools.foreach { p =>
          p.participants.clear()
          p.fights.clear()
        }

        round.pools.zipWithIndex.foreach { case (p, i) => p.participants ++= pools(i) }
        round.save
      }
    }
    def planSwissRound(round: Round) {
      if (finishedFights_?(round)) {
        S.notice("Cannot plan this round, because fights have been fought")
        //      } else if (round.order.is != 1) {
        //        S.notice("Cannot plan this round, because it is not the first round")
      } else {
        SwissTournament.planning(round)
        //        round.pools.foreach { p =>
        //          p.fights.clear
        //          var pts = p.participants.toList.sortBy(_ => Random.nextDouble())
        //          while (pts.size > 1) {
        //            val pt = pts.take(1)(0)
        //            pts -= pt
        //            var opponent = pts.find(pt2 => pt2.club.is != pt.club.is && pt2.isStarFighter.is != pt.isStarFighter.is)
        //            if (opponent.isEmpty)
        //              opponent = pts.find(pt2 => pt2.isStarFighter.is != pt.isStarFighter.is)
        //            if (opponent.isEmpty)
        //              opponent = Some(pts.take(1)(0))
        //            pts -= opponent.get
        //
        //            val f = Fight.create.fighterA(pt).fighterB(opponent.get)
        //            f.order(p.fights.size + 1)
        //            p.fights += f
        //          }
        //          p.save
        //        }
      }
    }

    def newPool(round: Round) {
      if (finishedFights_?(round)) {
        S.notice("Cannot add a poule, because fights have been fought")
      } else {
        val pool = Pool.create.order(round.pools.size + 1)
        round.pools += pool
        round.save

        if (pool.order.get > 1)
          redistributePools(round, List())
      }
    }
    def deletePool(round: Round, pool: Pool) {
      if (finishedFights_?(round)) {
        S.notice("Cannot delete a poule, because fights have been fought")
      } else {
        val surplusParticipants = pool.participants.toList
        round.pools -= pool
        round.pools.toList.zipWithIndex.foreach { case (p, i) => p.order(i + 1) }
        round.save
        redistributePools(round, surplusParticipants)
      }
    }
    def addFight(pool: Pool) {
      if (fighterA.isDefined && fighterB.isDefined) {
        if (fighterA.get.id.is == fighterB.get.id.is) {
          S.notice("Please select two different fighters")
        } else {
          val f = Fight.create.fighterA(fighterA).fighterB(fighterB)
          f.order(pool.fights.size + 1)
          pool.fights += f
          pool.save
          refresh(Some(pool))
        }
      } else {
        S.notice("Please select two fighters")
      }
    }
    def moveUp(fight: Fight) {
      Fight.find(By(Fight.order, fight.order.is - 1), By(Fight.pool, fight.pool.get)).foreach { p =>
        p.order(p.order.is + 1)
        fight.order(fight.order.is - 1)
        fight.save
        p.save
      }
    }
    def moveDown(fight: Fight) {
      Fight.find(By(Fight.order, fight.order.is + 1), By(Fight.pool, fight.pool.get)).foreach { n =>
        n.order(n.order.is - 1)
        fight.order(fight.order.is + 1)
        fight.save
        n.save
      }
    }
    def delete(pool: Pool, fight: Fight) {
      pool.fights -= fight
      pool.fights.toList.zipWithIndex.foreach { case (f, i) => f.order(i + 1) }
      pool.save
    }
    def deleteParticipantFromTournament(tournament: Tournament, participant: Participant) {
      tournament.participants -= participant
      tournament.save
      S.notice("%s has been removed from this tournament" format participant.name.is)
    }

    def refresh(anchor: Option[Any] = None) =
      S.redirectTo(t.id.is.toString + anchor.map {
        case p: Pool => "#poule" + p.id.is
        case r: Round => "#round" + r.id.is
        case _ => ""
      }.getOrElse(""))

    def unlock(fight: Fight) {
      fight.inProgress(false).save
      S.notice("Unlocked fight " + fight.shortLabel)
    }

    def edit(fight: Fight) {
      S.redirectTo("/fights/edit/" + fight.id.is)
    }

    def renderResults(f: Fight) = {
      if (f.inProgress.get) {
        Text("in progress")
      } else if (f.finished_?) {
        val s = f.currentScore
        <span><b title={ "%d points, %d afterblows against" format (s.a, s.aAfter) }>{ s.a }</b> (<span title={ "%d double hits" format s.double }>{ s.double }</span>) <b title={ "%d points, %d afterblows against" format (s.b, s.bAfter) }>{ s.b }</b></span>
      } else {
        Text("vs")
      }
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

    def allParticipantsFromPrevious(r: Round): List[(Box[Participant], String)] = {
      r.previousRound.map(_.pools.flatMap(_.participants.map(p => Full(p) -> p.name.is)).toList.sortBy(_._2)).openOr(List())
    }

    def removeFromPool(p: Pool, pt: Participant) {
      p.participants -= pt
      p.save
    }

    def participantsNotInThisRound(r: Round) = {
      (r.tournament.obj.get.participants.toList diff r.pools.flatMap(_.participants.toList).toList).sortBy(_.name.is)
    }

    def renderFightFighter(f: Participant) =
      if (f.clubCode.is == "")
        <span style="font-weight:bold" title={ f.name.is }>{ f.shortName.is }</span>
      else
        <span><span style="font-weight:bold" title={ f.name.is }>{ f.shortName.is }</span><span style="float:right" title={ f.club.is }>{ f.clubCode.is }</span></span>

    "#tournamentName *" #> t.name &
      "#tournamentParticipant" #> tournamentParticipants.map(pt =>
        "* [class]" #> (if (pt.isPresent.get && pt.isEquipmentChecked.get) "present" else if (!pt.isPresent.get) "not_present" else "not_checked") &
          "img [src]" #> ("/images/" + (if (pt.isStarFighter.get) "star" else "star_gray") + ".png") &
          "img [class]" #> ("star" + pt.id) &
          "img [onclick]" #> SHtml.ajaxInvoke(() => toggleStar(pt)) &
          "span" #> pt.name &
          "button" #> SHtml.ajaxButton(EntityRef("otimes"), () => { deleteParticipantFromTournament(t, pt); refresh() }, "title" -> "Remove from Tournament")) &
      "#newRoundName" #> SHtml.text("", newRound, "placeholder" -> "New Round") &
      "#tournamentRound" #> t.rounds.map(r =>
        ".moveParticipants" #> (if (r.order.is > 1) {
          "#advanceAll" #> SHtml.button(<span><img src="/images/group.png"/> All</span>, () => advance(r, All)) &
            "#advanceSelected" #> SHtml.button(<span><img src="/images/cut_red.png"/> Selected</span>, () => S.redirectTo("/tournaments/advance/" + r.id.is)) &
            "#advanceWinners" #> SHtml.button(<span><img src="/images/medal_gold_2.png"/> Winners</span>, () => advance(r, Winners)) &
            "#advanceSingle" #> (SHtml.selectObj((Empty -> "-- Just this one --") :: allParticipantsFromPrevious(r), Empty, { p: Box[Participant] => p.foreach(p => advance(r, Single(p))) }) ++ SHtml.submit("OK", () => ()))
        } else {
          "*" #> ""
        }) &
          "#roundName *" #> <span><a name={ "round" + r.id.is }></a>{ r.order + ": " + r.name }</span> &
          ".deleteRound [onclick]" #> SHtml.ajaxInvoke { () =>
            deleteRound(t, r)
            refresh()
          } &
          ".addPool [onclick]" #> SHtml.ajaxInvoke { () =>
            newPool(r)
            refresh()
          } &
          (if (r.order.is == 1) {
            ".reorder [onclick]" #> SHtml.ajaxInvoke { () =>
              redistributePools(r, Nil)
              refresh()
            }
          } else {
            ".reorder" #> ""
          }) &
          ".planSwiss [onclick]" #> SHtml.ajaxInvoke { () =>
            planSwissRound(r)
            refresh()
          } &
          "name=timeLimit" #> SHtml.ajaxText((r.timeLimitOfFight.get / 1000).toString, { time => r.timeLimitOfFight(time.toLong seconds); r.save; S.notice("Time limit saved") }, "type" -> "number") &
          "name=fightBreak" #> SHtml.ajaxText((r.breakInFightAt.get / 1000).toString, { time => r.breakInFightAt(time.toLong seconds); r.save; S.notice("Break time saved") }, "type" -> "number") &
          "name=exchangeLimit" #> SHtml.ajaxText(r.exchangeLimit.toString, { time => r.exchangeLimit(time.toInt); r.save; S.notice("Exchange limit saved") }, "type" -> "number") &
          "#roundPool" #> r.pools.map { p =>
            val pptsAlphabetic = p.participants.sortBy(_.name.is)
            val pptsRanking = SwissTournament.ranking(p)
            ".deletePool [onclick]" #> SHtml.ajaxInvoke { () =>
              deletePool(r, p)
              refresh()
            } &
              "#poolName *" #> <span><a name={ "poule" + p.id.is }></a>{ "Poule %d" format p.order.is }</span> &
              "#poolFight *" #> p.fights.map(f => {
                ".fightOrder *" #> f.order.is &
                  ".red *" #> renderFightFighter(f.fighterA.obj.get) &
                  ".results *" #> renderResults(f) &
                  ".blue *" #> renderFightFighter(f.fighterB.obj.get) &
                  "name=moveUp" #> SHtml.ajaxButton(EntityRef("uArr"), () => { moveUp(f); refresh() }, "title" -> "Move Up", (if (f.order == 1) "disabled" else "enabled") -> "true") &
                  "name=moveDown" #> SHtml.ajaxButton(EntityRef("dArr"), () => { moveDown(f); refresh() }, "title" -> "Move Down", (if (f.order == p.fights.size) "disabled" else "enabled") -> "true") &
                  "name=remove" #> SHtml.ajaxButton(EntityRef("otimes"), () => { delete(p, f); refresh() }, "title" -> "Remove") &
                  "name=unlock" #> SHtml.ajaxButton("unlock", () => { unlock(f); refresh(Some(p)) }, "title" -> "Unlock", (if (f.inProgress.is) "enabled" else "disabled") -> "true") &
                  "name=edit" #> SHtml.ajaxButton("edit", () => { edit(f); refresh(Some(p)) }, "title" -> "Edit", (if (f.inProgress.is) "disabled" else "enabled") -> "true")
              }) &
              ".poolParticipantListHeader" #> (
                "* [onclick]" #> SHtml.ajaxInvoke(() => Run("$('#poolParticipantList" + p.id.is + "').toggle()")) &
                "span" #> p.participants.size) &
                ".poolParticipantList [id]" #> ("poolParticipantList" + p.id.is) &
                "#poolParticipant" #> pptsRanking.zipWithIndex.map {
                  case ((pt, ps), i) =>
                    val hasFights = p.fights.exists(f => f.fighterA.is == pt.id.is || f.fighterB.is == pt.id.is)
                    "img [src]" #> ("/images/" + (if (pt.isStarFighter.get) "star" else "star_gray") + ".png") &
                      "img [class]" #> ("star" + pt.id) &
                      "img [onclick]" #> SHtml.ajaxInvoke(() => toggleStar(pt)) &
                      ".participantName *" #> pt.name.is &
                      SwissTournament.renderRankedFighter(i + 1, pt, ps) &
                      ".hasFights *" #> (if (hasFights) "" else "Not fighting") &
                      (if (!hasFights)
                        ".actions *" #> SHtml.ajaxButton(EntityRef("otimes"), () => Confirm("There is no way back!", SHtml.ajaxInvoke { () => removeFromPool(p, pt); RedirectTo("/tournaments/view/" + t.identifier.is) }._2.cmd), "title" -> "Remove from poule")
                      else
                        ".actions *" #> "")

                } &
                "#addPoolParticipant *" #> SHtml.ajaxSelect(("-1", "-- Add Participant --") :: participantsNotInThisRound(r).map(pt => (pt.id.is.toString, pt.name.is)), Full("-1"), { id =>
                  p.participants += Participant.findByKey(id.toLong).get
                  p.save
                  RedirectTo("/tournaments/view/" + t.identifier.is)
                }) &
                "#planParticipantA" #> SHtml.select(("-1", "-- Select Red --") :: pptsAlphabetic.map(pt => (pt.id.is.toString, pt.name.is)).toList, Full("-1"), id => fighterA = Participant.findByKey(id.toLong)) &
                "#planParticipantB" #> (SHtml.select(("-1", "-- Select Blue --") :: pptsAlphabetic.map(pt => (pt.id.is.toString, pt.name.is)).toList, Full("-1"), id => fighterB = Participant.findByKey(id.toLong)) ++
                  SHtml.submit("Plan", () => addFight(p)))
          }) &
      "#addParticipant" #> SHtml.ajaxSelect(("-1", "-- Add Participant --") :: otherParticipants.map(pt => (pt.id.is.toString, pt.name.is)).toList, Full("-1"), id => addParticipant(t, id.toLong))

  }

}