package nl.malienkolders.htm.admin.lib.exporter

import java.io.OutputStream
import nl.malienkolders.htm.lib.model.Tournament
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Cell
import scala.collection.JavaConversions._
import java.util.Date

object ResultsExporter extends ExcelExporter {

  case class Config(exportType: String, startRow: Int, tournaments: List[Tournament], columns: Map[String, Int])

  def readRow(row: Row, cellIndex: Int, acc: List[String]): List[String] = {
    row.getCell(cellIndex) match {
      case cell: Cell => readRow(row, cellIndex + 1, acc :+ cell.getStringCellValue())
      case _ => acc
    }
  }

  def readConfig(rows: Iterator[Row], acc: Config): Config = rows.hasNext match {
    case true =>
      val row = rows.next
      row.getCell(0).getStringCellValue() match {
        case "type" => readConfig(rows, acc.copy(exportType = row.getCell(1).getStringCellValue()))
        case "firstRow" => readConfig(rows, acc.copy(startRow = row.getCell(1).getNumericCellValue().toInt))
        case "tournaments" => readConfig(rows, acc.copy(tournaments = row.getCell(1).getStringCellValue() match {
          case "all" => Tournament.findAll
          case _ => Nil
        }))
        case "columns" => readConfig(rows, acc.copy(columns = Map(readRow(row, 1, List()).zipWithIndex: _*)))
        case _ => acc
      }
    case _ => acc
  }

  def doExport(out: OutputStream): Unit = {
    val workbook = loadWorkbook("results")
    val configSheet = workbook.getSheetAt(0)
    val config = readConfig(configSheet.rowIterator(), Config("fights", 2, Nil, Map()))

    workbook.removeSheetAt(0)
    workbook.setSheetName(0, config.exportType)
    val dataSheet = workbook.getSheetAt(0)

    var rowIndex = config.startRow - 1
    config.tournaments foreach { t =>
      t.rounds foreach { r =>
        r.pools foreach { p =>
          p.fights foreach { f =>
            val row = dataSheet.getOrCreateRow(rowIndex)
            config.columns foreach {
              case (columnName, columnIndex) =>
                val setter = columnName match {
                  case "tournament.name" => cellSetter(t.name.get) _
                  case "round.name" => cellSetter(r.name.get) _
                  case "pool.name" => cellSetter(if (r.pools.size > 1) p.poolName else "-") _
                  case "fighterA.id" => cellSetter(f.fighterA.obj.get.externalId.get.toInt) _
                  case "fighterA.name" => cellSetter(f.fighterA.obj.get.name.get) _
                  case "fighterA.club" => cellSetter(f.fighterA.obj.get.clubCode.get) _
                  case "fighterB.id" => cellSetter(f.fighterB.obj.get.externalId.get.toInt) _
                  case "fighterB.name" => cellSetter(f.fighterB.obj.get.name.get) _
                  case "fighterB.club" => cellSetter(f.fighterB.obj.get.clubCode.get) _
                  case "points.a" => cellSetter(f.currentScore.a) _
                  case "points.doubles" => cellSetter(f.currentScore.double) _
                  case "points.b" => cellSetter(f.currentScore.b) _
                  case "time.planned" => cellSetter(new Date(f.plannedStartTime)) _
                  case "time.start" => cellSetter(new Date(f.timeStart.get)) _
                  case "time.duration" => cellSetter(f.netDuration.get / 1000) _
                  case _ => cellSetter("") _
                }
                setter(row.getOrCreateCell(columnIndex))
            }
            rowIndex += 1
          }
        }
      }
    }

    workbook.write(out)
  }

  def cellSetter(value: String)(cell: Cell) = cell.setCellValue(value)
  def cellSetter(value: Int)(cell: Cell) = cell.setCellValue(value)
  def cellSetter(value: Date)(cell: Cell) = cell.setCellValue(value)
  def cellSetter(value: Long)(cell: Cell) = cell.setCellValue(value)

}