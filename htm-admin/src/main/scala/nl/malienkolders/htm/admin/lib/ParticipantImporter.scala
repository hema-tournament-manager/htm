package nl.malienkolders.htm.admin.lib

import scala.xml._
import java.net.URL
import scala.io.Source
import scala.util.matching._
import nl.malienkolders.htm.lib.model._
import nl.malienkolders.htm.admin.model._
import net.liftweb.mapper._
import net.liftweb.util._
import nl.htm.importer.DummyImporter
import nl.htm.importer.swordfish.Swordfish2013Importer
import nl.htm.importer.swordfish.SwordfishSettings
import nl.htm.importer.heffac.HeffacImporter
import nl.htm.importer.EmptySettings

object ParticipantImporter {

  val tournamentNames = List(
    "longsword_open" -> "Longsword Open",
    "longsword_ladies" -> "Longsword Ladies",
    "wrestling" -> "Wrestling",
    "sabre" -> "Sabre",
    "rapier_dagger" -> "Rapier & Dagger",
    "sword_buckler" -> "Sword & Buckler")

  def readTuplesFromFile(filename: String) = Source.fromInputStream(ParticipantImporter.getClass().getResourceAsStream(filename), "UTF-8").getLines().toList.map(line => line.split(" -> ") match {
    case Array(code, name) => code -> name
    case _ => "" -> ""
  })

  lazy val clubCode2Name = Map(readTuplesFromFile("clubcodes"): _*)

  lazy val clubName2Code = clubCode2Name.map { case (c, n) => (n, c) }

  lazy val replacements = Map(readTuplesFromFile("clubreplacements").map { case (o, r) => (o.toLowerCase(), r) }: _*)

  def nameReplacements = Map(("9", "F. v. d. Bussche-H.") :: ParticipantNameMapping.findAll.map(pnm => (pnm.externalId.is -> pnm.shortName.is)).toList: _*)

  def normalizeName(nameRaw: String) = {
    def normalizePart(part: String) = {
      val subparts = part.split("-")
      subparts.map(sb => if (sb.length() > 3) sb.take(1).toUpperCase() + sb.drop(1).toLowerCase() else sb).mkString("-")
    }
    val name = nameRaw.replaceAll("\\s+", " ").trim()
    val parts = name.split(" ").toList
    parts.map(normalizePart _).mkString(" ")
  }

  def shortenName(name: String) = {
    val allParts = name.split(" ")
    val uppercasedParts = allParts.takeWhile(_.charAt(0).isUpper)
    val initials = uppercasedParts.take(allParts.length - 1).map(_.charAt(0) + ".")
    (initials.toList.mkString + " " + allParts.drop(initials.length).mkString(" ")).replace(" van ", " v. ").replace(" von dem ", " v.d. ").replace(" von ", " v. ")
  }

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

  def doImport = {
    
    val noCountry = Country.find(By(Country.code2, "")).get

    val data = Swordfish2013Importer.doImport(SwordfishSettings("http://www.ghfs.se/swordfish-attendee.php", Country.findAll.map(c => c.code2.get -> c.name.get)))
      //HeffacImporter.doImport(new EmptySettings)
    
    for (i <- Arena.count to (data.arenas - 1)) {
      Arena.create.name("Arena " + (i + 1)).save()
    }

    val tournaments = if (Tournament.count == 0) {
      data.tournaments.map { case t => Tournament.create.name(t.name).identifier(t.id).saveMe }
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
      case (t, tps) =>
        tournaments.find(_.identifier == t.id).foreach(t => t.participants ++= tps.map(p => ps.find(_.name == p.name).get))
    }
    tournaments.foreach(_.save)
  }

}