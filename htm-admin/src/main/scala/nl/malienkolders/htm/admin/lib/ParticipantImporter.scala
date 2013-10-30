package nl.malienkolders.htm.admin.lib

import scala.xml._
import java.net.URL
import scala.io.Source
import scala.util.matching._
import nl.malienkolders.htm.lib.model._
import nl.malienkolders.htm.lib.rulesets.Ruleset
import nl.malienkolders.htm.lib.util.Helpers._
import nl.malienkolders.htm.admin.model._
import net.liftweb.mapper._
import net.liftweb.util._
import net.liftweb.common._
import TimeHelpers._
import nl.htm.importer.DummyImporter
import java.io.File
import nl.htm.importer.EventData
import java.text.SimpleDateFormat
import java.util.Date
import nl.malienkolders.htm.lib.model.Tournament

object ParticipantImporter {

  import nl.malienkolders.htm.admin.lib.Utils.Constants._
  
  val eliminationSizes = Map(
    "longsword_open" -> 32,
    "longsword_ladies" -> 8,
    "rapier" -> 8,
    "sabre" -> 16)

  val finalsNames = Map(
    32 -> "16th Finals",
    16 -> "8th Finals",
    8 -> "Quarter Finals",
    4 -> "Semi Finals",
    2 -> ROUND_NAME_FINAL)

  val timesAndArenas: Map[String, (String, Map[Int, (Int, String)])] = Map(
    "longsword_open" ->
      ("2013-11-01" ->
        Map(
          1 -> (1, "15:00"),
          2 -> (2, "15:00"),
          3 -> (3, "15:00"),
          4 -> (1, "15:35"),
          5 -> (2, "15:35"),
          6 -> (3, "15:35"),
          7 -> (1, "16:10"),
          8 -> (2, "16:10"),
          9 -> (3, "16:10"),
          10 -> (1, "16:50"),
          11 -> (2, "16:50"),
          12 -> (3, "16:50"),
          13 -> (1, "17:25"),
          14 -> (2, "17:25"),
          15 -> (3, "17:25"))),
    "longsword_ladies" ->
      ("2013-11-01" ->
        Map(
          1 -> (1, "09:30"),
          2 -> (1, "10:20"),
          3 -> (1, "10:55"))),
    "rapier" ->
      ("2013-11-02" ->
        Map(
          1 -> (1, "09:30"),
          2 -> (2, "09:30"),
          3 -> (1, "10:50"),
          4 -> (2, "10:50"))),
    "sabre" ->
      ("2013-11-01" ->
        Map(
          1 -> (2, "09:30"),
          2 -> (3, "09:30"),
          3 -> (2, "10:05"),
          4 -> (3, "10:05"),
          5 -> (2, "10:45"),
          6 -> (3, "10:45"),
          7 -> (2, "11:30"),
          8 -> (3, "11:30"))))

  def findArenaAndTime(t: Tournament, p: Pool): Option[(Int, String)] = {
    timesAndArenas.get(t.identifier.get) match {
      case Some(times) =>
        val date = times._1
        times._2.get(p.order.get.toInt)
      case _ => None
    }
  }

  def findStartTime(t: Tournament, r: Round, p: Pool): Long = findArenaAndTime(t, p) match {
    case Some(arenaAndTime) =>
      val date = timesAndArenas(t.identifier.get)._1
      val dateTime = s"$date ${arenaAndTime._2}:00"
      val extraTime = (r.order.get - 1) * r.timeLimitOfFight.get * (p.participants.size / 2).floor.toLong
      new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateTime).getTime() + extraTime
    case _ => 0
  }

  def findArena(t: Tournament, p: Pool): Box[Arena] = findArenaAndTime(t, p) match {
    case Some(arenaAndTime) =>
      Arena.find(By(Arena.name, "Arena " + arenaAndTime._1))
    case _ => Empty
  }

  def doImport(data: EventData) = {

    val noCountry = Country.find(By(Country.code2, "")).get

    Arena.bulkDelete_!!()
    // create as many arenas as are missing
    for (i <- 1 to data.arenas) {
      Arena.create.name("Arena " + i).save()
    }

    val tournaments = if (Tournament.count == 0) {
      data.tournaments.map { case t => Tournament.create.name(t.name).identifier(t.id).mnemonic(t.mnemonic).saveMe }
    } else {
      Tournament.findAll(OrderBy(Tournament.id, Ascending))
    }

    val ps = data.participants.map(p => Participant.create.externalId(p.sourceIds.head.id).
      name(p.name).
      shortName(p.shortName).
      club(p.club).
      clubCode(p.clubCode).
      country(Country.find(By(Country.code2, p.country)).getOrElse(noCountry)))

    // insert participant if it doesn't exist yet
    ps.foreach(_.save)

    // add participants to tournaments
    tournaments.foreach { t =>
      t.participants.clear
      t.save
    }
    data.subscriptions.foreach {
      case (tournament, subs) =>
        tournaments.find(_.identifier == tournament.id).foreach { t =>
          t.subscriptions ++= subs.map {
            case (sub, p) =>
              TournamentParticipants.create.participant(ps.find(_.externalId.get == p.sourceIds.head.id).get).fighterNumber(sub.number).primary(sub.primary).experience(sub.xp)
          }
          if (t.rounds.size == 0) {
            val round = Round.create.name("Round 1").
              order(1).
              ruleset(tournament.ruleset).
              timeLimitOfFight(180 seconds).
              breakDuration(0).
              breakInFightAt(0).
              timeBetweenFights(0 seconds).
              exchangeLimit(10)

            t.rounds += round

            t.save
          }
          val round = t.rounds.head

          subs.foreach {
            case (sub, p) =>
              sub.pool foreach { poolNr =>
                while (round.pools.size < poolNr && !(t.identifier.get == "ladies_longsword" && round.pools.size >= 3))
                  round.addPool
                round.pools.find(_.order.get == poolNr).foreach { pool =>
                  pool.participants += ps.find(_.externalId.get == p.sourceIds.head.id).get
                }
              }
          }

          for (pool <- round.pools) {
            pool.startTime(findStartTime(t, round, pool))
            pool.arena(findArena(t, pool))
          }

          Ruleset.ruleset(round.ruleset.get).foreach(_.planning(round))

          t.save
        }

    }

    for {
      t <- tournaments if t.identifier.get != "wrestling"
      r <- t.rounds.headOption
    } {
      println(t.name.get + " / Round" + r.order.get)

      def numberOfRounds(poolSize: Int) = if (poolSize.isOdd) {
        poolSize
      } else {
        poolSize - 1
      }

      for (i <- 2 to numberOfRounds(r.pools.head.participants.size)) {
        val round = Round.create.name("Round " + i).
          order(i).
          ruleset(r.ruleset.get).
          timeLimitOfFight(180 seconds).
          breakDuration(0).
          breakInFightAt(0).
          timeBetweenFights(0 seconds).
          exchangeLimit(10)
        for (p <- r.pools) {
          val pool = Pool.create(t).order(p.order.get)
          pool.participants ++= p.participants
          round.pools += pool

          pool.startTime(findStartTime(t, round, pool))
          pool.arena(findArena(t, pool))
        }

        t.rounds += round

        t.save

        Ruleset.ruleset(round.ruleset.get).foreach(_.planning(round))

        t.save
      }
    }

    for {
      tournament <- tournaments
      size <- eliminationSizes.get(tournament.identifier.get)
    } {
      generateEliminationRound(tournament, size)
    }

    tournaments.foreach(_.save)

  }

  def generateEliminationRound(tournament: Tournament, size: Int): Unit = if (size >= 2) {
    if (size == 2) {
      tournament.rounds += Round.create.
        order(tournament.rounds.size + 1).
        name(ROUND_NAME_THIRD_PLACE).
        timeLimitOfFight(360).
        breakInFightAt(180).
        exchangeLimit(0).
        breakDuration(60).
        timeBetweenFights(0).
        ruleset(Ruleset().id)
    }

    tournament.rounds += Round.create.
      order(tournament.rounds.size + 1).
      name(finalsNames(size)).
      timeLimitOfFight(if (size == 2) 360 else 180).
      breakInFightAt(if (size == 2) 180 else 0).
      exchangeLimit(if (size == 2) 0 else 10).
      breakDuration(if (size == 2) 60 else 0).
      timeBetweenFights(0).
      ruleset(Ruleset().id)

    object pools
    generateEliminationRound(tournament, size / 2)
  }

}