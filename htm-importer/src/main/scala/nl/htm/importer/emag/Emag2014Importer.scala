package nl.htm.importer
package emag

import java.io.InputStream
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet

case class EmagExcelSettings(in: InputStream, countries: List[(String, String)])

object Emag2014Importer extends Importer[EmagExcelSettings] {
  implicit def cellToString(cell: Cell): String = if (cell == null) "" else cell.getStringCellValue()
  implicit def cellToInt(cell: Cell): Int = cell.getNumericCellValue().toInt

  def doImport(settings: EmagExcelSettings): EventData = {
    val workbook = WorkbookFactory.create(settings.in)

    val total = workbook.getSheetAt(0)

    val headerRow = total.getRow(1)

    val header = Map((for (i <- 0 to headerRow.getLastCellNum() - 1) yield (headerRow.getCell(i).getStringCellValue(), i)): _*)

    val tournaments = List(
      "longsword",
      "rapier",
      "dussack",
      "albion")

    val clubs = Map(
      "" -> "",
      "AMEK" -> "AMEK",
      "Blood and Iron" -> "B&I",
      "GHFS" -> "GHFS",
      "Maza de Plata" -> "MdP",
      "Krigerskole" -> "KS",
      "Caballeros de Ebano" -> "CdE",
      "EFC" -> "EFC",
      "Orden de Pendragon" -> "OdP",
      "Phoebus Ferratus" -> "PF",
      "Kanaan" -> "KNN")

    val subscriptionsRaw = for (rowIndex <- 2 to total.getLastRowNum() if total.getRow(rowIndex) != null && total.getRow(rowIndex).getCell(0) != null) yield {
      val row = total.getRow(rowIndex)
      (Participant(
        List(SourceId("EMAG", row.getCell(header("#")).getNumericCellValue().toInt.toString)),
        normalizeName(row.getCell(header("Name"))),
        shortenName(normalizeName(row.getCell(header("Name")))),
        row.getCell(header("School")),
        clubs(row.getCell(header("School"))),
        row.getCell(header("Country")).replace("?", ""),
        "") -> (for (colIndex <- header("Longsword") to header("Albion") if row.getCell(colIndex) != null) yield { tournaments(colIndex - header("Longsword")) }).toList)
    }

    val subscriptions = subscriptionsRaw.filter(_._1.name.length() > 0)

    EventData(2, subscriptions.map(_._1).toList, Nil, tournaments.map(t =>
      Tournament(t, t, t, t) -> subscriptions.filter(_._2.contains(t)).zipWithIndex.map {
        case ((p, s), i) =>
          Subscription(false, i + 1, 0, None) -> p
      }.toList).toMap)
  }
}