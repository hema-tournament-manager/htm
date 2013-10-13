package nl.malienkolders.htm.lib

import nl.malienkolders.htm.lib.model._
import nl.malienkolders.htm.lib.HtmHelpers._
import net.liftweb._
import net.liftweb.mapper._
import net.liftweb.http._
import net.liftweb.util.Helpers._
import net.liftweb.common.Box.box2Option
import net.liftweb.util.StringPromotable.intToStrPromo

package swiss {

  case class ParticipantScores(
      initialRanking: Int,
      fights: Int,
      wins: Int,
      ties: Int,
      losses: Int,
      cleanHitsReceived: Int,
      cleanHitsDealt: Int,
      specialHitsReceived: Int,
      specialHitsDealt: Int,
      afterblowsReceived: Int,
      afterblowsDealt: Int,
      doubleHits: Int) {
    def points = wins * 1 + ties * 0.5
    def group = if (fights > 0) points else -10 + initialRanking
    def hitsReceived = cleanHitsReceived + specialHitsReceived + afterblowsReceived + afterblowsDealt + doubleHits
    def firstHits = cleanHitsDealt + specialHitsDealt + afterblowsDealt
  }

}

import swiss.ParticipantScores

object SwissTournament extends nl.malienkolders.htm.lib.Tournament {

  val id = "swiss"

  type Scores = ParticipantScores

  def compare(rapierRules: Boolean)(s1: ParticipantScores, s2: ParticipantScores)(implicit random: scala.util.Random) = {
    (s1, s2) match {
      case (ParticipantScores(i1, f1, w1, t1, l1, cr1, c1, sr1, sd1, _, _, d1), ParticipantScores(i2, f2, w2, t2, l2, cr2, c2, sr2, sd2, _, _, d2)) =>
        if (f1 == 0 && f2 == 0) {
          // if both haven't fought yet: order by initial ranking
          i1 > i2
        } else if (f1.min(f2) == 0) {
          // put fighters who have fought before those who haven't
          f2 == 0
        } else {
          // rank according to Phil's document
          if (s1.points != s2.points) {
            // a. highest score
            s1.points > s2.points
          } else if (s1.hitsReceived != s2.hitsReceived) {
            // b. fewest total hits received
            s1.hitsReceived < s2.hitsReceived
          } else if (c1 != c2) {
            // c. most clean hits scored
            c1 > c2
          } else if (d1 != d2) {
            // d. fewest doubles
            d1 < d2
          } else if ((rapierRules && (sd1 != sd2)) || (!rapierRules && (s1.firstHits != s2.firstHits))) {
            // e. Most “superior” hits
            //  i. This is most 2pt hits in rapier
            //  ii. Or most first hits if using after-blow rules in other weapons
            if (rapierRules)
              sd1 > sd2
            else
              s1.firstHits > s2.firstHits
          } else if (cr1 != cr2) {
            // f. fewest clean hits received
            cr1 < cr2
          } else if (s1.firstHits != s2.firstHits) {
            // g. most first hits
            s1.firstHits > s2.firstHits
          } else if (w1 != w2) {
            // h. most wins
            w1 > w2
          } else {
            // i. randomly
            random.nextBoolean
          }
        }
    }
  }

  def ranking(p: Pool): List[(Participant, ParticipantScores)] = {
    // seed the Random with the pool id, so the random ranking is always the same for this pool
    implicit val random = new scala.util.Random(p.id.is)
    val pts = p.participants.toList
    val r = p.round.obj.get
    val t = r.tournament.obj.get
    val rs = Round.findAll(By(Round.tournament, t)).filter(_.order.is <= r.order.is)
    val ps = Pool.findAll(ByList(Pool.round, rs.map(_.id.is)))
    val fs = Fight.findAll(ByList(Fight.pool, ps.map(_.id.is))).filter(_.finished_?)
    pts.map(pt => (pt -> fs.foldLeft(ParticipantScores(pt.initialRanking, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)) {
      case (ps @ ParticipantScores(i, c, w, t, l, hR, hD, sR, sD, aR, aD, d), f) =>
        if (f.inFight_?(pt)) {
          f.currentScore match {
            case TotalScore(a, aafter, b, bafter, double, aspec, bspec, _) if a == b && f.fighterA.is == pt.id.is =>
              ParticipantScores(i, c + 1, w, t + 1, l, hR + b, hD + a, sR + bspec, sD + aspec, aR + aafter, aD + bafter, d + double)
            case TotalScore(a, aafter, b, bafter, double, aspec, bspec, _) if a == b && f.fighterB.is == pt.id.is =>
              ParticipantScores(i, c + 1, w, t + 1, l, hR + a, hD + b, sR + aspec, sD + bspec, aR + bafter, aD + aafter, d + double)
            case TotalScore(a, aafter, b, bafter, double, aspec, bspec, _) if a > b && f.fighterA.is == pt.id.is =>
              ParticipantScores(i, c + 1, w + 1, t, l, hR + b, hD + a, sR + bspec, sD + aspec, aR + aafter, aD + bafter, d + double)
            case TotalScore(a, aafter, b, bafter, double, aspec, bspec, _) if a > b && f.fighterB.is == pt.id.is =>
              ParticipantScores(i, c + 1, w, t, l + 1, hR + a, hD + b, sR + aspec, sD + bspec, aR + bafter, aD + aafter, d + double)
            case TotalScore(a, aafter, b, bafter, double, aspec, bspec, _) if a < b && f.fighterA.is == pt.id.is =>
              ParticipantScores(i, c + 1, w, t, l + 1, hR + b, hD + a, sR + bspec, sD + aspec, aR + aafter, aD + bafter, d + double)
            case TotalScore(a, aafter, b, bafter, double, aspec, bspec, _) if a < b && f.fighterB.is == pt.id.is =>
              ParticipantScores(i, c + 1, w + 1, t, l, hR + a, hD + b, sR + aspec, sD + bspec, aR + bafter, aD + aafter, d + double)
            case _ => ParticipantScores(i, c, w, t, l, hR, hD, sR, sD, aR, aD, d)
          }
        } else {
          ps
        }
    })).sortWith((pt1, pt2) => compare(t.rapier_?)(pt1._2, pt2._2))
  }

  def ranking(r: Round): List[(Pool, List[(Participant, ParticipantScores)])] = {
    r.pools.toList.map { p =>
      (p, ranking(p))
    }
  }

  def haveFoughtBefore(prevPools: List[Pool], a: Participant, b: Participant) = {
    Fight.find(ByList(Fight.pool, prevPools.map(_.id.is)), By(Fight.fighterA, a.id.is), By(Fight.fighterB, b.id.is)).isDefined ||
      Fight.find(ByList(Fight.pool, prevPools.map(_.id.is)), By(Fight.fighterA, b.id.is), By(Fight.fighterB, a.id.is)).isDefined
  }

  def planning(round: Round): List[Pool] = {
    val pr = round.previousRound
    val ppr = pr.map(_.previousRound)
    val prs = round.previousRounds
    val ranked = ranking(round)
    ranked.map {
      case (p, pts) =>
        val pp = pr.map(_.pools.find(_.order.is == p.order.is).get)
        var grouped = pts.groupBy { case (_, s) => s.group }.map { case (g, pts) => (g, pts.map(_._1)) }
        val groupKeys = grouped.keys.toList.sorted.reverse
        // put the person who received a bye in the previous round at the top of the lowest group
        if (pp.isDefined && pts.size.odd_?) {
          val previousByes = prs.flatMap { pr =>
            pr.pools.map { pp =>
              pp.participants.find { pt =>
                !pp.fights.exists(f => f.fighterA.is == pt.id.is || f.fighterB.is == pt.id.is)
              }.get
            }
          }
          grouped = grouped.filterNot(_._1 == groupKeys.last).map {
            case (g, pts) =>
              (g, pts filterNot (_.id.is == previousByes.head.id.is))
          } ++ List((grouped.find(_._1 == groupKeys.last).get match {
            case (g, pts) => (g, previousByes.head :: (pts filterNot (_.id.is == previousByes.head.id.is)))
          }))
        }
        // drop the last (= lowest ranked) fighter if group size is odd
        if (pts.size.odd_?) {
          grouped = grouped.filterNot(_._1 == groupKeys.last) + (grouped.find(_._1 == groupKeys.last).get match { case (g, pts) => (g, pts.dropRight(1)) })
        }
        var passToNextGroup: Option[Participant] = None
        for (key <- groupKeys) yield {
          var group = passToNextGroup.toList ++ grouped(key)
          while (group.size > 1) {
            val a = group.head
            val b = group.drop(1).reverse.find(b => !haveFoughtBefore(prs.flatMap(_.pools.toList), b, a) && b.clubCode.is != a.clubCode.is).
              orElse(group.drop(1).reverse.find(b => !haveFoughtBefore(prs.flatMap(_.pools.toList), b, a))).getOrElse(group.drop(1).head)
            p.addFight(a, b)
            group = group.filterNot(pt => pt.id.is == a.id.is || pt.id.is == b.id.is)
          }
          if (group.size == 1) {
            passToNextGroup = Some(group.head)
          } else {
            passToNextGroup = None
          }
        }
        p.saveMe
    }
  }

  def renderRankedFighter(rank: Int, p: Participant, s: ParticipantScores) =
    ".ranking *" #> rank &
      ".name *" #> p.name &
      ".club [title]" #> p.club &
      ".club *" #> p.clubCode &
      ".initial *" #> s.initialRanking &
      ".fights *" #> s.fights &
      ".points *" #> s.points.toString &
      ".hitsReceived *" #> s.hitsReceived.toString &
      ".cleanHitsDealt *" #> s.cleanHitsDealt.toString &
      ".doubleHits *" #> s.doubleHits.toString &
      ".specialHitsDealt *" #> s.specialHitsDealt.toString &
      ".cleanHitsReceived *" #> s.cleanHitsReceived.toString &
      ".firstHitsDealt *" #> s.firstHits.toString &
      ".wins *" #> s.wins.toString

}