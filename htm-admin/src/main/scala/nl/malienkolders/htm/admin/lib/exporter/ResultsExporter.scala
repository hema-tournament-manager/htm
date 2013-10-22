package nl.malienkolders.htm.admin.lib.exporter

import java.io.OutputStream
import nl.malienkolders.htm.lib.model._
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Cell
import scala.collection.JavaConversions._
import java.util.Date
import org.apache.poi.ss.usermodel.Sheet
import net.liftweb.mapper._
import org.apache.poi.ss.usermodel.CellStyle

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
          case "all" => Tournament.findAll(OrderBy(Tournament.id, Ascending)).filter(_.identifier.get != "melee")
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
    implicit val config = readConfig(configSheet.rowIterator(), Config("fights", 2, Nil, Map()))

    workbook.removeSheetAt(0)
    workbook.setSheetName(0, config.exportType)
    implicit val dataSheet = workbook.getSheetAt(0)

    implicit var rowIndex = config.startRow - 1
    config.tournaments foreach { implicit t =>
      printTournament(t)
      val poolPhaseRounds = t.rounds.filter(isPoolPhaseRound _)
      poolPhaseRounds.head.pools foreach { pool =>
        printPool(pool)
        poolPhaseRounds foreach { r =>
          printRound(r)
          r.pools.find(_.order.get == pool.order.get) foreach { p =>
            p.fights foreach { f =>
              printFight(f)
              rowIndex += 1
            }
          }
        }
      }
      rowIndex += 1
      val finalRounds = t.rounds.filterNot(isPoolPhaseRound _)
      finalRounds foreach { r =>
        printRow(Map("pool.name" -> r.name.get))
        val style = workbook.createCellStyle()
        style.setAlignment(CellStyle.ALIGN_LEFT)
        dataSheet.getRow(rowIndex).getCell(config.columns("pool.name")).setCellStyle(style)
        r.pools foreach { p =>
          p.fights foreach { f =>
            printFight(f)
            rowIndex += 1
          }
        }
        rowIndex += 1
      }
      rowIndex += 1
    }

    workbook.write(out)
  }

  def isPoolPhaseRound(r: Round): Boolean = r.name.get.startsWith("Ronde ")

  def printTournament(tournament: Tournament)(implicit sheet: Sheet, rowIndex: Int, config: Config) =
    printRow(Map("tournament.name" -> tournament.name.get))

  def printPool(pool: Pool)(implicit sheet: Sheet, rowIndex: Int, config: Config) =
    printRow(Map("pool.name" -> pool.poolName))

  def printRound(round: Round)(implicit sheet: Sheet, rowIndex: Int, config: Config) =
    printRow(Map("round.name" -> round.name.get))

  def printFight(f: Fight)(implicit sheet: Sheet, rowIndex: Int, config: Config) =
    printRow(Map("fighterA.id" -> f.fighterA.obj.get.externalId.get.toInt,
      "fighterA.name" -> f.fighterA.obj.get.name.get,
      "fighterA.club" -> f.fighterA.obj.get.clubCode.get,
      "fighterB.id" -> f.fighterB.obj.get.externalId.get.toInt,
      "fighterB.name" -> f.fighterB.obj.get.name.get,
      "fighterB.club" -> f.fighterB.obj.get.clubCode.get,
      "points.a" -> f.currentScore.a,
      "points.doubles" -> f.currentScore.double,
      "points.b" -> f.currentScore.b,
      "time.planned" -> new Date(f.plannedStartTime),
      "time.start" -> new Date(f.timeStart.get),
      "time.duration" -> f.netDuration.get / 1000))

  def printRow(data: Map[String, Any])(implicit sheet: Sheet, rowIndex: Int, config: Config) = {
    data foreach {
      case (column, value) =>
        if (config.columns.containsKey(column)) {
          val cell = sheet.getOrCreateRow(rowIndex).getOrCreateCell(config.columns(column))
          value match {
            case i: Int => cell.setCellValue(i)
            case d: Date => cell.setCellValue(d)
            case l: Long => cell.setCellValue(l)
            case a: Any => cell.setCellValue(a.toString)
          }
        }
    }
  }

}