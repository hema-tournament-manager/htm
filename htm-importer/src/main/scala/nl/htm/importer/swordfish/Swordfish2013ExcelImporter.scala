package nl.htm.importer
package swordfish

import nl.htm.importer._
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet

case class SwordfishExcelSettings(in: InputStream, countries: List[(String, String)])

object Swordfish2013ExcelImporter extends Importer[SwordfishExcelSettings] {

  import Swordfish2013Importer._

  implicit def cellToString(cell: Cell): String = if (cell == null) "" else cell.getStringCellValue()
  implicit def cellToInt(cell: Cell): Int = cell.getNumericCellValue().toInt

  def doImport(settings: SwordfishExcelSettings): EventData = {
    val workbook = WorkbookFactory.create(settings.in)

    val total = workbook.getSheet("Total")

    val headerRow = total.getRow(0)

    val header = Map((for (i <- 0 to headerRow.getLastCellNum() - 1) yield (headerRow.getCell(i).getStringCellValue(), i)): _*)

    val tournaments = Swordfish2013Importer.tournamentNames.map { case (id, (name, mnemonic)) => Tournament(id, name, mnemonic, "swordfish-2013-" + (if (id == "rapier") "rapier" else "default")) }
    println(total.getLastRowNum())

    println(settings.countries)

    println("Importing participants")
    val participants = for (rowIndex <- 1 to total.getLastRowNum() if total.getRow(rowIndex).getCell(0) != null) yield {
      val row = total.getRow(rowIndex)
      val countryNameRaw = row.getCell(header("Country")) match {
        case cell: Cell => cell.getStringCellValue()
        case _ => ""
      }
      val countryName = Swordfish2013Importer.countryReplacements.getOrElse(countryNameRaw, countryNameRaw)
      println(countryName)
      val country = settings.countries.find { case (_, name) => countryName == name }.map(_._1).getOrElse("")
      val (clubCode, clubName) = normalizeClub(row.getCell(header("Club")))
      Participant(
        List(SourceId("swordfish2013", row.getCell(header("ID")).getNumericCellValue().toInt.toString)),
        normalizeName(row.getCell(header("Name"))),
        shortenName(normalizeName(row.getCell(header("Name")))),
        clubName,
        clubCode,
        country,
        row.getCell(header("T-Shirt")))
    }

    val subscriptions = tournaments.flatMap {
      case t @ Tournament(_, name, _, _) =>
        println("Importing tournament " + name)
        val sheet = workbook.getSheet(name)
        if (sheet != null) {
          val poolFighterNumbers = findPoolFighterNumbers(sheet)
          val subscriptions = for (rowIndex <- 2 to sheet.getLastRowNum() if sheet.getRow(rowIndex).getCell(0) != null && sheet.getRow(rowIndex).getCell(1) != null && sheet.getRow(rowIndex).getCell(5) != null) yield {
            val row = sheet.getRow(rowIndex)
            val (primary, xp) = Swordfish2013Importer.parseSubscriptionString(row.getCell(5))
            row.getCell(1).getNumericCellValue().toInt.toString -> Subscription(primary, row.getCell(0), xp, poolFighterNumbers.get(row.getCell(0).getNumericCellValue().toInt))
          }
          val myParticipants = subscriptions.flatMap {
            case (id, sub) =>
              participants.find(_.sourceIds.head.id == id) map { p =>
                sub -> p
              }
          }
          Some(t -> myParticipants.toList)
        } else {
          None
        }
    } :+ (tournaments.find(_.id == "wrestling").get -> findWrestlers(total, header, participants))

    EventData(3, participants.toList, tournaments, subscriptions.toMap)
  }

  def getPoolNumberFromCell(cell: Cell): Option[Int] = cell match {
    case c: Cell if c.getStringCellValue().startsWith("Pool ") =>
      Some(c.getStringCellValue().dropWhile(!_.isDigit).toInt)
    case _ => None
  }

  def findPoolColumns(row: Row, columnIndex: Int, acc: Map[Int, Int] = Map()): Map[Int, Int] = {
    if (columnIndex > row.getLastCellNum()) {
      acc
    } else {
      findPoolColumns(row, columnIndex + 1, getPoolNumberFromCell(row.getCell(columnIndex)).map(poolNr => acc + (poolNr -> columnIndex)).getOrElse(acc))
    }
  }

  def findPoolFighterNumbers(sheet: Sheet): Map[Int, Int] = {
    val poolColumns = findPoolColumns(sheet.getRow(0), 0).dropRight(if (sheet.getSheetName() == "Longsword - Ladies") 1 else 0)
    poolColumns.flatMap {
      case (poolNr, columnIndex) =>
        val options = for (i <- 2 to sheet.getLastRowNum()) yield {
          val row = sheet.getRow(i)
          row.getCell(columnIndex) match {
            case c: Cell => Some(c.getNumericCellValue().toInt -> poolNr)
            case _ => None
          }
        }
        options.flatten.toList
    } toMap
  }

  def findWrestlers(sheet: Sheet, header: Map[String, Int], participants: Seq[Participant]): List[(Subscription, Participant)] = {
    val columnIndex = header("Wrestling")
    var fighterNr = 0
    val subs = for (i <- 1 to sheet.getLastRowNum() if sheet.getRow(i).getCell(columnIndex) != null) yield {
      val row = sheet.getRow(i)
      val id = row.getCell(header("ID")).getNumericCellValue().toInt.toString
      val (primary, xp) = Swordfish2013Importer.parseSubscriptionString(row.getCell(columnIndex))
      val p = participants.find(_.sourceIds.head.id == id)
      p.map { p =>
        fighterNr += 1
        Subscription(primary, fighterNr, xp) -> p
      }
    }
    subs.flatten.toList
  }

}