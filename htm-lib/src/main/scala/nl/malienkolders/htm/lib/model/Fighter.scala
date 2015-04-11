package nl.malienkolders.htm.lib.model

import net.liftweb.common._


sealed abstract class Fighter {
  def format: String
  def participant: Option[Participant]
  def sameAs(other: Fighter): Boolean
 
}
object Fighter {
  def parse(s: String): Fighter = Winner.parse(s)
    .orElse(Loser.parse(s))
    .orElse(PoolFighter.parse(s))
    .orElse(SpecificFighter.parse(s))
    .getOrElse(UnknownFighter(s))
}
case class Winner(fight: Either[EliminationFight, FreeStyleFight]) extends Fighter {
  def format = "F" + fight.fold(_.phaseType.code, _.phaseType.code) + fight.fold(_.id, _.id).get + "W"
  def participant = fight.fold(_.finished_?, _.finished_?) match {
    case true => fight.fold(_.winner, _.winner)
    case false => None
  }
  def sameAs(other: Fighter) = other match {
    case w: Winner => fight.fold(_.id, _.id).get == w.fight.fold(_.id, _.id).get
    case _ => false
  }
  override def toString = "Winner of " + fight.fold(_.name, _.name).is
}
object Winner extends (Either[EliminationFight, FreeStyleFight] => Winner) {
  val re = """^F(E|F)(\d+)W$""".r

  def apply(ef: EliminationFight): Winner = apply(scala.Left(ef))
  def apply(ff: FreeStyleFight): Winner = apply(scala.Right(ff))

  def parse(s: String): Option[Winner] = re.findFirstIn(s) match {
    case Some(re(phaseType, fightId)) =>
      phaseType match {
        case "E" => EliminationFight.findByKey(fightId.toLong).toOption.map(f => Winner(f))
        case "F" => FreeStyleFight.findByKey(fightId.toLong).toOption.map(f => Winner(f))
      }
    case None => None
  }
}
case class Loser(fight: Either[EliminationFight, FreeStyleFight]) extends Fighter {
  def format = "F" + fight.fold(_.phaseType.code, _.phaseType.code) + fight.fold(_.id, _.id).get + "L"
  def participant = fight.fold(_.finished_?, _.finished_?) match {
    case true => fight.fold(_.loser, _.loser)
    case false => None
  }
  def sameAs(other: Fighter) = other match {
    case l: Loser => fight.fold(_.id, _.id).get == l.fight.fold(_.id, _.id).get
    case _ => false
  }
  override def toString = "Loser of " + fight.fold(_.name, _.name).get
}
object Loser extends (Either[EliminationFight, FreeStyleFight] => Loser) {
  val re = """^F(E|F)(\d+)L$""".r

  def apply(ef: EliminationFight): Loser = apply(scala.Left(ef))
  def apply(ff: FreeStyleFight): Loser = apply(scala.Right(ff))

  def parse(s: String): Option[Loser] = re.findFirstIn(s) match {
    case Some(re(phaseType, fightId)) =>
      phaseType match {
        case "E" => EliminationFight.findByKey(fightId.toLong).toOption.map(f => Loser(f))
        case "F" => FreeStyleFight.findByKey(fightId.toLong).toOption.map(f => Loser(f))
      }
    case None => None
  }
}
case class PoolFighter(pool: Pool, ranking: Int) extends Fighter {
  def format = "P" + pool.id.get + ":" + ranking
  def participant = {
    println(s"POOL ${pool.poolName} FINISHED: ${pool.finished_?}")
    pool.finished_? match {
      case true =>
        pool.ranked match {
          case ranked if ranked.size >= ranking => Some(ranked(ranking - 1)._1)
          case _ => None
        }
      case false => None
    }
  }
  def sameAs(other: Fighter) = other match {
    case pf: PoolFighter => pool.id.get == pf.pool.id.get && ranking == pf.ranking
    case _ => false
  }
  override def toString = "Number " + ranking + " of pool " + pool.poolName
}
object PoolFighter extends ((Pool, Int) => PoolFighter) {
  val re = """^P(\d+):(\d+)$""".r

  def parse(s: String): Option[PoolFighter] = re.findFirstIn(s) match {
    case Some(re(poolId, ranking)) => Pool.findByKey(poolId.toLong) match {
      case Full(p) => Some(PoolFighter(p, ranking.toInt))
      case _ => None
    }
    case None => None
  }
}
case class SpecificFighter(override val participant: Option[Participant]) extends Fighter {
  def format = participant.map(_.id.get.toString).getOrElse("PICK")
  def sameAs(other: Fighter) = other match {
    case SpecificFighter(Some(otherParticipant)) => participant.map(_.id.is == otherParticipant.id.is).getOrElse(false)
    case _ => false
  }
  override def toString = participant.map(_.name.get).getOrElse("")
}
object SpecificFighter extends (Option[Participant] => SpecificFighter) {
  val re = """^(\d+)$""".r

  def parse(s: String): Option[SpecificFighter] = re.findFirstIn(s) match {
    case Some(re(participantId)) => Some(SpecificFighter(Some(Participant.findByKey(participantId.toLong)).get))
    case None if s == "PICK" => Some(SpecificFighter(None))
    case None => None
  }
}
case class UnknownFighter(label: String) extends Fighter {
  def format = label
  def participant = None
  def sameAs(other: Fighter) = false
  override def toString = label
}