package nl.htm.importer
package random

import scala.io.Source
import scala.util.Random

case class RandomSettings(participantCount: Int, countryCodes: List[String], tournamentIdentifiers: List[String])

object RandomImporter extends Importer[RandomSettings] {

  private def readFile(name: String) = Source.fromInputStream(getClass.getResourceAsStream(name)).getLines().toList

  def doImport(settings: RandomSettings): EventData = {
    val allFirstNames = readFile("firstnames-female") ++ readFile("firstnames-male")
    val allLastNames = readFile("surnames")
    val firstNames = Random.shuffle(allFirstNames).take(settings.participantCount)
    val lastNames = Random.shuffle(allLastNames).take(settings.participantCount)
    val clubNames = readFile("../swordfish/clubcodes").map(_.split(" -> ")).map(x => x(0) -> x(1))

    val participants = for (((f, l), i) <- (firstNames zip lastNames) zipWithIndex) yield {
      val (clubName, clubCode) = clubNames(Random.nextInt(clubNames.size))
      Participant(
        List(SourceId("RANDOM", (i + 1).toString)),
        normalizeName(f + " " + l),
        shortenName(normalizeName(f + " " + l)),
        clubCode,
        clubName,
        settings.countryCodes(Random.nextInt(settings.countryCodes.size)),
        "")
    }

    val participantTournaments = participants.map(p => p -> Random.shuffle(settings.tournamentIdentifiers).take(Random.nextInt(2) + 1))

    val subscriptions = settings.tournamentIdentifiers.map { t =>
      Tournament(t, t, t, t) -> participantTournaments.filter(_._2.contains(t)).zipWithIndex.map {
        case ((p, ts), i) =>
          Subscription(ts.head == t, i + 1, 0, None) -> p
      }
    }

    EventData(2, participants, Nil, Map(subscriptions: _*))
  }
}