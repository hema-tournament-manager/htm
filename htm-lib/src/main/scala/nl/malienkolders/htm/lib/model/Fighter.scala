package nl.malienkolders.htm.lib.model

import net.liftweb.common._

case class MarshalledFighter(label: String, participant: Option[MarshalledParticipant])

sealed abstract class Fighter {
  def format: String
  def participant: Option[Participant]
  def sameAs(other: Fighter): Boolean
  def toMarshalled = MarshalledFighter(toString, participant.map(_.toMarshalled))
}
object Fighter {
  def parse(s: String): Fighter = Winner.parse(s)
    .orElse(Loser.parse(s))
    .orElse(PoolFighter.parse(s))
    .orElse(SpecificFighter.parse(s))
    .getOrElse(UnknownFighter(s))
}
case class Winner(fight: EliminationFight) extends Fighter {
  def format = "F" + fight.id.get + "W"
  def participant = fight.finished_? match {
    case true => fight.winner
    case false => None
  }
  def sameAs(other: Fighter) = other match {
    case w: Winner => fight.id.get == w.fight.id.get
    case _ => false
  }
  override def toString = "Winner of " + fight.name.is
}
object Winner extends (EliminationFight => Winner) {
  val re = """^F(\d+)W$""".r

  def parse(s: String): Option[Winner] = re.findFirstIn(s) match {
    case Some(re(fightId)) => Some(Winner(EliminationFight.findByKey(fightId.toLong).get))
    case None => None
  }
}
case class Loser(fight: EliminationFight) extends Fighter {
  def format = "F" + fight.id.get + "L"
  def participant = fight.finished_? match {
    case true => fight.loser
    case false => None
  }
  def sameAs(other: Fighter) = other match {
    case l: Loser => fight.id.get == l.fight.id.get
    case _ => false
  }
  override def toString = "Loser of " + fight.name.is
}
object Loser extends (EliminationFight => Loser) {
  val re = """^F(\d+)L$""".r

  def parse(s: String): Option[Loser] = re.findFirstIn(s) match {
    case Some(re(fightId)) => Some(Loser(EliminationFight.findByKey(fightId.toLong).get))
    case None => None
  }
}
case class PoolFighter(pool: Pool, ranking: Int) extends Fighter {
  def format = "P" + pool.id.get + ":" + ranking
  def participant = {
    println(s"POOL ${pool.poolName} FINISHED: ${pool.finished_?}")
    pool.finished_? match {
      case true => Some(pool.ranked(ranking - 1)._1)
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