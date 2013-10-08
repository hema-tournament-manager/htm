package nl.htm.importer

import scala.util.Random

case class Tournament(id: String, name: String)

case class SourceId(source: String, id: String)

case class Participant(sourceIds: List[SourceId], name: String, shortName: String, club: String, clubCode: String, country: String)

case class EventData(participants: List[Participant], tournaments: List[Tournament], subscriptions: Map[Tournament, List[Participant]])

abstract class Importer[Settings] {
  implicit def tuple2sourceId(t: (String, String)): SourceId = SourceId(t._1, t._2)

  def doImport(s: Settings): EventData;
}

case class EmptySettings()

object DummyImporter extends Importer[EmptySettings] {
  override def doImport(s: EmptySettings = EmptySettings()): EventData = {
    val r = new Random(1)
    val ps = List("Bas van Meel", "Jogchem Dijkstra", "Maarten Kamphuis", "Mattias Ryrlen", "Pieter Peeters", "Tim de Jager", "Youval Kuipers") map (p => Participant(List("dummy" -> p.toLowerCase().replaceAll("[^a-z]", "").take(8)), p, p, "Orde der Noorderwind", "nw", "NL"))
    val ts = List(Tournament("longsword_open", "Open Longsword"), Tournament("rapier_dagger", "Rapier & Dagger"))
    EventData(ps, ts, Map(ts.map(t => t -> (ps filter (_ => r.nextDouble < 0.75))): _*))
  }
}