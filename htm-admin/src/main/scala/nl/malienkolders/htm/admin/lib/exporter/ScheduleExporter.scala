package nl.malienkolders.htm.admin.lib.exporter

import nl.malienkolders.htm.lib.model._
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.FileOutputStream
import java.io.OutputStream
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Sheet
import java.util.Date

object ScheduleExporter extends ExcelExporter {

  abstract sealed class PoolHeaderStyle
  case object ShortHeaders extends PoolHeaderStyle
  case object LongHeaders extends PoolHeaderStyle

  case class Block(name: String, fights: Seq[Fight[_, _]])

  def doExport(header: String, blocks: Seq[Block], headerStyle: PoolHeaderStyle, outputStream: OutputStream): Unit = {
    val workbook = loadWorkbook("schedule")

    val sheet = workbook.getSheetAt(0)

    sheet.getOrCreateRow(0).getOrCreateCell(0).setCellValue(header)

    var i = 2

    for (block <- blocks) {

      if (i == 2) {
        i = i + 1
      }
      val headerRow = sheet.getOrCreateRow(i)

      headerRow.getOrCreateCell(0).setCellValue(block.name)
      i = i + 1;

      val fights = block.fights.groupBy(_.scheduled.foreign.isDefined)

      for {
        scheduledFights <- fights.get(true)
        fight <- scheduledFights
      } {
        val row = sheet.getOrCreateRow(i)

        row.getOrCreateCell(1).setCellValue(new Date(fight.scheduled.foreign.get.time.get))
        renderFight(row, fight)

        i = i + 1
      }

      for {
        unscheduledFights <- fights.get(false)
        fight <- unscheduledFights
      } {
        val row = sheet.getOrCreateRow(i)

        row.getOrCreateCell(1).setCellValue("??:??")
        renderFight(row, fight)

        i = i + 1
      }

    }

    workbook.write(outputStream)
  }

  private def renderFight(row: Row, fight: Fight[_, _]) = {
    val t = fight.tournament
    row.getOrCreateCell(0).setCellValue(fight match {
      case pf: PoolFight =>
        pf.pool.foreign.get.poolName
      case ef: EliminationFight =>
        ef.phase.foreign.get.fights.count(_.round.is == ef.round.is) match {
          case n if n > 1 => s"1/$n"
          case _ => ""
        }
    })
    row.getOrCreateCell(2).setCellValue(fight.fighterA.participant.map(_.subscription(t).get.fighterNumber.get.toString).getOrElse("?"))
    row.getOrCreateCell(3).setCellValue(fight.fighterA.participant.map(f => f.shortName.get + " (" + f.clubCode.get + ")").getOrElse(fight.fighterA.toString))
    row.getOrCreateCell(4).setCellValue(fight.fighterB.participant.map(_.subscription(t).get.fighterNumber.get.toString).getOrElse("?"))
    row.getOrCreateCell(5).setCellValue(fight.fighterB.participant.map(f => f.shortName.get + " (" + f.clubCode.get + ")").getOrElse(fight.fighterB.toString))
  }

  def doExport(tournament: Tournament)(outputStream: OutputStream): Unit = {
    doExport(tournament.name.get, tournament.phases.map(p => Block(p.name.get, p.fights)), ShortHeaders, outputStream)
  }

  def doExport(arena: Arena, onlyUnfinishedPools: Boolean)(outputStream: OutputStream) {
    //TODO: fix arena export
  }

}