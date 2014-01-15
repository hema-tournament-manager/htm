package nl.htm.importer.swordfish

import nl.htm.importer._
import scala.io.Source
import scala.Array.canBuildFrom
import java.net.URL
import scala.util.matching.Regex

case class SwordfishSettings(url: String, countries: List[(String, String)])

object Swordfish2013Importer extends Importer[SwordfishSettings] {

  val tournamentNames = List(
    "longsword_open" -> ("Longsword - Open", "LS"),
    "longsword_ladies" -> ("Longsword - Ladies", "LSL"),
    "wrestling" -> ("Wrestling", "WRS"),
    "sabre" -> ("Sabre", "SAB"),
    "rapier" -> ("Rapier", "RAP"))

  lazy val clubCode2Name = Map(readTuplesFromFile("clubcodes"): _*)

  lazy val clubName2Code = clubCode2Name.map { case (c, n) => (n, c) }

  lazy val replacements = Map(readTuplesFromFile("clubreplacements").map { case (o, r) => (o.toLowerCase(), r) }: _*)

  lazy val countryReplacements = Map(readTuplesFromFile("countryreplacements"): _*)

  def parseSubscriptionString(s: String): (Boolean, Int) = {
    val re = """X(p|s)(\**)""".r
    val re(primary, xp) = s
    (primary == "p", xp.length())
  }

  def nameReplacements = Map("9" -> "F. v. d. Bussche-H.")

  def normalizeClub(clubRaw: String) = {
    def uppercaseWord(word: String) = if (word.length() > 3 && !word.contains(".")) word.take(1).toUpperCase() + word.drop(1) else word
    val club = clubRaw.replaceAll("\\s+", " ").trim()
    println("club: " + club)
    val replaced: String = replacements.getOrElse(club.toLowerCase(), club)
    println("replaced: " + replaced)
    val uppercased = replaced.split(" ").map(uppercaseWord _).mkString(" ")
    println("uppercased: " + uppercased)
    if (uppercased == "" || uppercased == "-")
      ("", "")
    else if (clubCode2Name.contains(uppercased))
      (uppercased, clubCode2Name(uppercased))
    else if (clubName2Code.contains(uppercased))
      (clubName2Code(uppercased), uppercased)
    else
      ("", uppercased)
  }

  override def doImport(s: SwordfishSettings = SwordfishSettings("http://www.ghfs.se/swordfish-attendee.php", List())): EventData = {
    val noCountry = ""

    val tournaments = tournamentNames.map { case (id, (name, mnemonic)) => Tournament(id, name, mnemonic, "swordfish-2013-" + (if (id == "rapier") "rapier" else "default")) }

    val data = Source.fromURL(new URL(s.url), "UTF-8").getLines.mkString.replaceAll("[\n\t\r]+", "")

    val entry = new Regex("""<tr><td bgcolor="[^"]+">(\d+)</td>""" +
      """<td bgcolor="[^"]+">([^<]+)</td>""" +
      """<td bgcolor="[^"]+">([^<]*)</td>""" +
      """<td bgcolor="[^"]+" align="center">([^<]*)</td>""" +
      """<td bgcolor="[^"]+">([^<]*)</td>""" +
      """<td bgcolor="[^"]+" align="center">([^<]*)</td>""" +
      """<td bgcolor="[^"]+" align="center">([^<]*)</td>""" +
      """<td bgcolor="[^"]+" align="center">([^<]*)</td>""" +
      """<td bgcolor="[^"]+" align="center">([^<]*)</td>""" +
      """<td bgcolor="[^"]+" align="center">([^<]*)</td>""" +
      """<td bgcolor="[^"]+" align="center">(X?)</td>""" +
      """<td bgcolor="[^"]+" align="center">(X?)</td></tr>""")

    val entries: List[(Participant, List[String])] = (for (
      entry(id, name, club, countryNameRaw, shirt, longsword, longswordLadies, wrestling, sabre, rapier, judge, crew) <- entry findAllIn data
    ) yield {
      val (clubCode, clubName) = normalizeClub(club)
      val countryName = countryReplacements.get(countryNameRaw).getOrElse(countryNameRaw)
      val country = s.countries.find { case (_, name) => countryName == name }.map(_._1).getOrElse(noCountry)
      val p = Participant(
        List(SourceId("swordfish", id)),
        normalizeName(name),
        nameReplacements.get(id).getOrElse(shortenName(normalizeName(name))),
        clubName,
        clubCode,
        country,
        shirt)
      (p, List(longsword, longswordLadies, wrestling, sabre, rapier))
    }).toList

    val registrations: List[(Tournament, Participant)] = entries.flatMap {
      case (p, ts) =>
        // find all tournaments that this person has registered for 
        ts.zipWithIndex.filter(_._1 startsWith ("X")).map {
          case (_, index) =>
            // map them to a tuple describing this registration
            tournaments(index) -> p
        }
    }

    // group the registrations together by Tournament and collect the participants in a list
    val registrationsGrouped: Map[Tournament, List[(Subscription, Participant)]] = registrations.groupBy(_._1).map {
      case (t, ps) =>
        (t, ps.map(p => Subscription(true, p._2.sourceIds.head.id.toInt, 0) -> p._2))
    }

    EventData(3, entries.map(_._1), tournaments, registrationsGrouped)
  }

}