package nl.malienkolders.htm.admin.lib

import nl.malienkolders.htm.lib.model._
import net.liftweb.mapper.By
import nl.malienkolders.htm.admin.lib.Utils.PimpedParticipant

object Marshallers {

  case class MarshalledTournamentSummary(id: Long, name: String, memo: String)
  case class MarshalledTournamentRound(id: Long, finished: Boolean)
  case class MarshalledTournament(id: Option[Long], name: String, memo: String, participants: List[Long])
  case class MarshalledTournamentSummaryV3(id: Option[Long], name: String, memo: String, participants: Option[Int])
  case class MarshalledTournamentV3(id: Long, name: String,
    memo: String, participants: Seq[MarshalledParticipantV3], phases: List[MarshalledPhaseV3], fights: List[MarshalledFightV3])

  case class MarshalledPhaseV3(id: Long, tournament: Long, phaseType: String, name: String, pools: Option[List[MarshalledPoolV3]], fights: List[Long])
  case class MarshalledFightV3(id: Long, phase: Long, name: String, fighterA: MarshalledFighterV3, fighterB: MarshalledFighterV3)

  case class MarshalledPoolV3(id: Long, name: String, fights: List[Long])
  case class MarshalledFighterV3(winnerOf: Option[Long], loserOf: Option[Long], participant: Option[Long])

  case class MarshalledFighter(label: String, participant: Option[MarshalledParticipant])

  case class MarshalledScoring(points: String, effect: String)

  case class MarshalledHit(name: String, scoreType: String, side: String, scorings: List[MarshalledScoring])

  case class MarshalledFight(
    tournament: MarshalledTournamentSummary,
    phaseType: String,
    id: Long,
    name: String,
    fighterA: MarshalledFighter,
    fighterB: MarshalledFighter,
    timeStart: Long,
    timeStop: Long,
    netDuration: Long,
    scores: List[MarshalledScore],
    timeLimit: Long,
    exchangeLimit: Int,
    possiblePoints: List[Int],
    doubleHitLimit: Int,
    breakAt: Long,
    breakDuration: Long,
    pointLimit: Int,
    possibleHits: List[MarshalledHit])

  case class MarshalledScore(
    timeInFight: Long,
    timeInWorld: Long,
    pointsRed: Int,
    pointsBlue: Int,
    afterblowsRed: Int,
    afterblowsBlue: Int,
    cleanHitsRed: Int,
    cleanHitsBlue: Int,
    doubles: Int,
    exchanges: Int,
    scoreType: String)

  case class MarshalledPoolSummary(id: Long, name: String, order: Long, startTime: Long, finished: Boolean, fightCount: Long, participantsCount: Long)
  case class MarshalledPool(id: Long, name: String, startTime: Long, order: Long, fights: List[Long], participants: List[MarshalledParticipant])
  case class MarshalledScheduledFightSummary(time: Long, fight: MarshalledFight)

  case class MarshalledParticipant(

    id: Option[Long],
    externalId: String,
    name: String,
    shortName: String,
    club: String,
    clubCode: String,
    country: String,
    isPresent: Boolean,
    tshirt: String,
    age: Int,
    height: Int,
    weight: Int,
    previousWins: List[String],
    fighterNumber: Option[Int],
    gearChecked: Option[Boolean],
    droppedOut: Option[Boolean],
    pool: Option[String],
    subscriptions: List[MarshalledSubscription],
    hasPicture: Option[Boolean])

  case class MarshalledParticipantV3(
    id: Option[Long],
    name: String,
    shortName: String,
    club: MarshalledClub,
    country: MarshalledCountry,
    isPresent: Boolean,
    tshirt: Option[String],
    age: Option[Int],
    height: Option[Int],
    weight: Option[Int],
    previousWins: List[String],
    subscriptions: List[MarshalledSubscriptionV3],
    hasPicture: Boolean)

  case class MarshalledSubscription(
    fighterNumber: Int,
    gearChecked: Boolean,
    droppedOut: Boolean,
    pool: Option[String],
    tournament: MarshalledTournamentSummary)

  case class MarshalledSubscriptionV3(
    fighterNumber: Int,
    gearChecked: Boolean,
    droppedOut: Boolean,
    tournament: Long)
  case class MarshalledViewerPoolFightSummary(order: Long, fighterA: MarshalledParticipant, fighterB: MarshalledParticipant, started: Boolean, finished: Boolean, score: TotalScore)

  implicit class MarshallingScore(s: Score[_, _]) {

    def toMarshalled =
      MarshalledScore(s.timeInFight.is, s.timeInWorld.is, s.pointsRed.is, s.pointsBlue.is,
        s.cleanHitsRed.is, s.cleanHitsBlue.is, s.afterblowsRed.is, s.afterblowsBlue.is,
        s.doubles.is, s.exchanges.is, s.scoreType.is)

    def fromMarshalled(m: MarshalledScore) = {
      s.timeInFight(m.timeInFight)
      s.timeInWorld(m.timeInWorld)
      s.pointsRed(m.pointsRed)
      s.pointsBlue(m.pointsBlue)
      s.cleanHitsRed(m.cleanHitsRed)
      s.cleanHitsBlue(m.cleanHitsBlue)
      s.afterblowsRed(m.afterblowsRed)
      s.afterblowsBlue(m.afterblowsBlue)
      s.doubles(m.doubles)
      s.exchanges(m.exchanges)
      s.scoreType(m.scoreType)
      this
    }
  }

  implicit class MarshallingPhase(p: Phase[_]) {

    def toMarshalledV3 = MarshalledPhaseV3(
      p.id.is,
      p.tournament.is,
      p.phaseType.code,
      p.name.is,
      p match {
        case pool: PoolPhase => Some(pool.pools.map(_.toMarshalledV3).toList)
        case _ => None
      },
      p.fights.map(_.id.is).toList)

  }

  implicit class MarshallingTournament(t: Tournament) {

    def toMarshalled = MarshalledTournament(
      Some(t.id.is),
      t.name.is,
      t.mnemonic.is,
      t.participants.map(_.id.is).toList)

    def toMarshalledSummary = MarshalledTournamentSummary(t.id.is, t.name.is, t.mnemonic.is)

    def toMarshalledSummaryV3 = MarshalledTournamentSummaryV3(
      Some(t.id.is),
      t.name.is,
      t.mnemonic.is,
      Some(t.participants.size))

    def toMarshalledV3 = MarshalledTournamentV3(
      t.id.is,
      t.name.is,
      t.mnemonic.is,
      t.participants.map(_.toMarshalledV3),
      t.phases.map(phase => MarshallingPhase(phase).toMarshalledV3).toList,
      t.fights.map(fight => MarshallingFight(fight).toMarshalledV3).toList)
  }

  implicit class MarshallingParticipant(p: Participant) {

    def toMarshalledV3 = MarshalledParticipantV3(
      Some(p.id.is),
      p.name.is,
      p.shortName.is,
      MarshalledClub(None, p.clubCode.is, p.club.is),
      p.country.obj.map(_.toMarshalled).get,
      p.isPresent.is,
      Some(p.tshirt.is),
      Some(p.age.is),
      Some(p.height.is),
      Some(p.weight.is),
      p.previousWins.is.split("""(\n|\r)+""").toList,
      p.subscriptions.map(_.toMarshalledV3).toList,
      p.hasAvatar)

    def toMarshalled = MarshalledParticipant(
      Some(p.id.is),
      p.externalId.is,
      p.name.is,
      p.shortName.is,
      p.club.is,
      p.clubCode.is,
      p.country.obj.map(_.code2.is).getOrElse(""),
      p.isPresent.is,
      p.tshirt.is,
      p.age.is,
      p.height.is,
      p.weight.is,
      p.previousWins.is.split("""(\n|\r)+""").toList,
      None,
      None,
      None,
      None,
      p.subscriptions.map(sub =>
        MarshalledSubscription(
          sub.fighterNumber.get,
          sub.gearChecked.get,
          sub.droppedOut.get,
          p.poolForTournament(sub.tournament.foreign.get).map(_.poolName),
          sub.tournament.foreign.get.toMarshalledSummary)).toList,
      None)

    def fromMarshalled(m: MarshalledParticipant) = {
      p.externalId(m.externalId)
      p.name(m.name)
      p.shortName(m.shortName)
      p.club(m.club)
      p.clubCode(m.clubCode)
      p.isPresent(m.isPresent)
      p.tshirt(m.tshirt)
      p.age(m.age)
      p.height(m.height)
      p.weight(m.weight)
      p.previousWins(m.previousWins.mkString("\n"))
      p.country(Country.find(By(Country.code2, m.country)))
    }

    def fromMarshalledV3(m: MarshalledParticipantV3) = {
      p.name(m.name)
      p.shortName(m.shortName)
      p.club(m.club.name)
      p.clubCode(m.club.code)
      p.isPresent(m.isPresent)
      m.tshirt.map(t => p.tshirt(t))
      m.age.map(a => p.age(a))
      m.height.map(h => p.height(h))
      m.weight.map(w => p.weight(w))
      p.previousWins(m.previousWins.mkString("\n"))
      p.country(Country.find(By(Country.code2, m.country.code2)))
    }
  }

  implicit class MarshallingTournamentParticipant(p: TournamentParticipant) {

    def toMarshalled = p.participant.foreign.get.toMarshalled.copy(
      fighterNumber = Some(p.fighterNumber.get),
      gearChecked = Some(p.gearChecked.get),
      droppedOut = Some(p.droppedOut.get),
      pool = p.participant.foreign.get.poolForTournament(p.tournament.foreign.get).map(_.poolName))

    def toMarshalledV3 = MarshalledSubscriptionV3(
      p.fighterNumber.get,
      p.gearChecked.get,
      p.droppedOut.get,
      p.tournament.foreign.map(_.id.is).getOrElse(0))
  }

  implicit class MarshallingFight(p: Fight[_, _]) {

    def toMarshalled = {
      val ruleset = p.phase.foreign.get.rulesetImpl
      val fp = ruleset.fightProperties
      MarshalledFight(
        p.phase.foreign.get.tournament.foreign.get.toMarshalledSummary,
        p.phaseType.code,
        p.id.is,
        p.name.is,
        p.fighterA.toMarshalled(p.phase.foreign.get.tournament.foreign.get),
        p.fighterB.toMarshalled(p.phase.foreign.get.tournament.foreign.get),
        p.timeStart.is,
        p.timeStop.is,
        p.netDuration.is,
        p.mapScores(_.toMarshalled).toList,
        fp.timeLimit,
        fp.exchangeLimit,
        ruleset.possiblePoints,
        fp.doubleHitLimit,
        fp.breakAt,
        fp.breakDuration,
        fp.pointLimit,
        fp.possibleHits.map(h => MarshalledHit(h.name, h.scoreType, h.side.serialized, h.scorings.map(s => MarshalledScoring(s.points.serialized, s.effect.serialized)))))
    }

    def toMarshalledV3 = MarshalledFightV3(
      p.id.is,
      p.phase.is,
      p.name.is,
      p.fighterA.toMarshalledV3,
      p.fighterB.toMarshalledV3)

    //    def fromMarshalled(m: MarshalledFight) = {
    //      p.timeStart(m.timeStart)
    //      p.timeStop(m.timeStop)
    //      p.netDuration(m.netDuration)
    //      p.scores.clear
    //      m.scores.foreach(s => p.addScore(p.createScore.fromMarshalled(s)))
    //      this
    //    }

  }

  implicit class MarshallingPoolFight(p: PoolFight) {

    def toViewerSummary = MarshalledViewerPoolFightSummary(
      p.order.is,
      p.fighterA.participant.get.toMarshalled,
      p.fighterB.participant.get.toMarshalled,
      p.started_?,
      p.finished_?,
      p.currentScore)

  }

  implicit class MarshallingFighter(f: Fighter) {

    def toMarshalled(tournament: Tournament) = MarshalledFighter(toString, for {
      p <- f.participant
      s <- p.subscription(tournament)
    } yield s.toMarshalled)

    def toMarshalledV3 = MarshalledFighterV3(
      f match {
        case winner: Winner => Some(winner.fight.fold(_.id.is, _.id.is))
        case _ => None
      },
      f match {
        case loser: Loser => Some(loser.fight.fold(_.id.is, _.id.is))
        case _ => None
      },
      f match {
        case pool: PoolFighter => Some(pool.participant.map(_.id.is).getOrElse(0));
        case specific: SpecificFighter => Some(specific.participant.map(_.id.is).getOrElse(0));
        case _ => None
      })

  }

  implicit class MarshallingPool(f: Pool) {
    def toMarshalled = MarshalledPool(f.id.is, f.poolName, f.startTime.is, f.order.is, f.fights.map(_.id.is).toList, f.participants.map(_.toMarshalled).toList)
    def toMarshalledSummary = MarshalledPoolSummary(
      f.id.is,
      f.poolName,
      f.order.is,
      f.startTime.is,
      f.finished_?,
      f.fights.size,
      f.participants.size)
      
    def toMarshalledV3 = MarshalledPoolV3(f.id.is, f.poolName, f.fights.map(_.id.is).toList)

  }

  //  implicit class MarshallingScheduledFight[F <: ScheduledFight[F]](f: ScheduledFight[F]) {
  //    def toMarshalledSummary = MarshalledScheduledFightSummary(f.time.is, MarshallingFight(f.fight.obj.get).toMarshalled(m))
  //  }

}