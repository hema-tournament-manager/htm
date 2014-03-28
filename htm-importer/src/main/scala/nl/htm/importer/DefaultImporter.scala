package nl.htm.importer

import java.io.InputStream
import org.apache.poi.ss.usermodel.WorkbookFactory
import nl.htm.importer.emag.EmagExcelSettings
import org.apache.poi.ss.usermodel.Cell

case class DefaultSettings(in: InputStream)

object DefaultImporter extends Importer[DefaultSettings] {

  implicit def cellToString(cell: Cell): String = if (cell == null) "" else cell.getStringCellValue()
  implicit def cellToInt(cell: Cell): Int = cell.getNumericCellValue().toInt

  def doImport(settings: DefaultSettings): EventData = {
    val workbook = WorkbookFactory.create(settings.in)

    val participants = workbook.getSheetAt(0)

    val headerRow = participants.getRow(0)

    val tournaments = (for (i <- 6 to headerRow.getLastCellNum() - 1) yield headerRow.getCell(i).getStringCellValue()).toList

    val subscriptionsRaw = for (rowIndex <- 1 to participants.getLastRowNum() if participants.getRow(rowIndex) != null && participants.getRow(rowIndex).getCell(0) != null) yield {
      val row = participants.getRow(rowIndex)
      (Participant(
        List(SourceId("DEFAULT", row.getCell(0).getNumericCellValue().toInt.toString)),
        row.getCell(1),
        row.getCell(2),
        row.getCell(3),
        row.getCell(4),
        row.getCell(5),
        "") -> (for (colIndex <- 6 to headerRow.getLastCellNum() - 1 if row.getCell(colIndex) != null) yield { tournaments(colIndex - 6) }).toList)
    }

    val subscriptions = subscriptionsRaw.filter(_._1.name.length() > 0)

    EventData(2, subscriptions.map(_._1).toList, Nil, tournaments.map(t =>
      Tournament(t, t, t, t) -> subscriptions.filter(_._2.contains(t)).zipWithIndex.map {
        case ((p, s), i) =>
          Subscription(false, i + 1, 0, None) -> p
      }.toList).toMap)
  }

}