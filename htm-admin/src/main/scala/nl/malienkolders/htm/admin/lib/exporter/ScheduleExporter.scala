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
import nl.malienkolders.htm.admin.lib.Utils.DateTimeRenderHelper
import org.apache.poi.ss.util.WorkbookUtil

object ScheduleExporter extends ExcelExporter {

  case class Group(name: String, blocks: Seq[Block])
  case class Block(name: String, fights: Seq[Fight[_, _]])

  def doExport(groups: Seq[Group], outputStream: OutputStream): Unit = {
    val workbook = loadWorkbook("schedule")

    while (workbook.getNumberOfSheets() < groups.size) {
      workbook.cloneSheet(0)
    }

    for ((group, s) <- groups.zipWithIndex) {
      val sheet = workbook.getSheetAt(s)
      workbook.setSheetName(s, WorkbookUtil.createSafeSheetName(group.name))
      sheet.getOrCreateRow(0).getOrCreateCell(0).setCellValue(group.name)

      var i = 2

      for (block <- group.blocks) {

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

          row.getOrCreateCell(1).setCellValue(fight.scheduled.foreign.get.time.get.hhmm)
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
    }
    workbook.write(outputStream)
  }

  private def renderFight[F <: Fight[_, _]](row: Row, fight: F) = {
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
    doExport(Seq(Group(tournament.name.get, tournament.phases.map(p => Block(p.name.get, p.fights)))), outputStream)
  }

  def doExport(arena: Arena, onlyUnfinishedPools: Boolean)(outputStream: OutputStream) {
    doExport((arena.timeslotByDay.zipWithIndex.map {
      case ((day, timeslots), i) =>
        Group(s"Day ${i + 1} - ${arena.name}", timeslots.map(ts =>
          Block(s"${ts.from.get.hhmm}-${ts.to.get.hhmm} ${ts.name.get}", ts.fights.map(_.fight.foreign.get))))
    }).toSeq, outputStream)
  }

}