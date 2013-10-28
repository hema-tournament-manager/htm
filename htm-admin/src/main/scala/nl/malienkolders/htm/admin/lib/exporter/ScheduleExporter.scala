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

  def doExport(header: String, pools: Seq[Pool], poolHeaders: PoolHeaderStyle, outputStream: OutputStream): Unit = {
    val workbook = loadWorkbook("schedule");

    val sheet = workbook.getSheetAt(0);

    sheet.getOrCreateRow(0).getOrCreateCell(0).setCellValue(header);

    var i = 2;

    for (pool <- pools) {

      if (poolHeaders == ShortHeaders) {
        i = i + 1;
        val poolRow = sheet.getOrCreateRow(i);
        poolRow.getOrCreateCell(0).setCellValue(pool.poolName)
      } else {
        if (i == 2)
          i = i + 1;
        val headerRow = sheet.getOrCreateRow(i);
        val r = pool.round.obj.get
        val t = r.tournament.obj.get
        headerRow.getOrCreateCell(0).setCellValue(t.name.get + " / " + r.name.get + " / " + pool.poolName)
        i = i + 1;
      }

      for (fight <- pool.fights) {

        val row = sheet.getOrCreateRow(i);

        row.getOrCreateCell(1).setCellValue(new Date(fight.plannedStartTime))
        row.getOrCreateCell(2).setCellValue(fight.fighterA.foreign.get.subscription(pool).get.fighterNumber.get)
        row.getOrCreateCell(3).setCellValue(fight.fighterA.foreign.map(f => f.shortName.get + " (" + f.clubCode.get + ")").get)
        row.getOrCreateCell(4).setCellValue(fight.fighterB.foreign.get.subscription(pool).get.fighterNumber.get)
        row.getOrCreateCell(5).setCellValue(fight.fighterB.foreign.map(f => f.shortName.get + " (" + f.clubCode.get + ")").get)

        i = i + 1;
      }
    }

    workbook.write(outputStream);
  }

  def doExport(tournament: Tournament)(outputStream: OutputStream): Unit = {
    doExport(tournament.name.get, tournament.rounds.flatMap(_.pools).sortBy(_.startTime.get), ShortHeaders, outputStream);
  }

  def doExport(arena: Arena, onlyUnfinishedPools: Boolean)(outputStream: OutputStream) {
    doExport(arena.name.get, arena.pools.sortBy(_.startTime.get).filter(a => !onlyUnfinishedPools || !a.finished_?), LongHeaders, outputStream);
  }

}