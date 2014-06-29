package nl.htm.importer

import java.io.InputStream
import org.apache.poi.ss.usermodel.WorkbookFactory
import nl.htm.importer.emag.EmagExcelSettings
import org.apache.poi.ss.usermodel.Cell

case class DefaultSettings(in: InputStream)

object DefaultImporter extends Importer[DefaultSettings] {

  implicit def cellToString(cell: Cell): String = if (cell == null) "" else if (cell.getCellType() == Cell.CELL_TYPE_STRING) cell.getStringCellValue() else cell.getNumericCellValue().toInt.toString
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
        "") -> (for (colIndex <- 6 to headerRow.getLastCellNum() - 1 if row.getCell(colIndex) != null) yield { tournaments(colIndex - 6) -> cellToString(row.getCell(colIndex)) }).toList)
    }

    val subscriptions = subscriptionsRaw.filter(_._1.name.length() > 0)

    EventData(2, subscriptions.map(_._1).toList, Nil, tournaments.map(t =>
      Tournament(t, t, t, t) -> subscriptions.filter(_._2.map(_._1).contains(t)).zipWithIndex.map {
        case ((p, s), i) =>
          // get the mapping from tournament name to pool number/letter
          val tournamentSubscription = s.find(_._1 == t).get
          Subscription(false, i + 1, 0, poolNumber(tournamentSubscription._2)) -> p
      }.toList).toMap)
  }

  private def poolNumber(value: String): Option[Int] = value match {
    // in the documentation X is used to mark a subscription, so it can't be used as a pool letter
    case "X" => None
    case int if int.matches("""^\d+$""") => Some(int.toInt)
    case letter if letter.length() == 1 => Some(letter.charAt(0).toInt - 'A'.toInt + 1)
    case _ => None
  }

}