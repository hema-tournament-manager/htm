package nl.htm.importer.heffac

import nl.htm.importer._
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File

object HeffacImporter extends Importer[EmptySettings] {

  val tournamentNames = List(
    "feder" -> "Feder",
    "nylon" -> "Langzwaard Nylon",
    "melee" -> "Melee Games")
  
  def doImport(s: EmptySettings): EventData = {
    val workbook = WorkbookFactory.create(new File("/home/jogchem/heffaf-inschrijvingen.xlsx"))

    val sheet = workbook.getSheetAt(0)

    val headerRow = sheet.getRow(0)

    val headers = Map((for (i <- 0 to (headerRow.getLastCellNum() - 1)) yield headerRow.getCell(i).getStringCellValue() -> i): _*)

    val participants: List[Participant] = (for (i <- 1 to (sheet.getLastRowNum() - 1)) yield {
      val row = sheet.getRow(i)
      if (row.getCell(headers("voornaam")) != null) {
        val index = row.getCell(headers("aantal")).getNumericCellValue().toInt.toString
        val voornaam = row.getCell(headers("voornaam")).getStringCellValue()
        val achternaam = row.getCell(headers("achternaam")).getStringCellValue()
        val club = row.getCell(headers("ben je lid van een HEMA vereniging?")).getStringCellValue()
        Some(Participant(List(SourceId("heffac", index)), voornaam + " " + achternaam, voornaam.take(1) + ". " + achternaam, club, "", "NL"))
      } else {
        None
      }
    }).toList.filter(_.isDefined).map(_.get)
    
    val tournaments = tournamentNames.map { case (id, name) => Tournament(id, name) }

    EventData(participants, tournaments, Map())
  }

}