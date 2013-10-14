package nl.malienkolders.htm.admin.lib.exporter

import nl.malienkolders.htm.lib.model.Tournament
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.FileOutputStream
import java.io.OutputStream
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Sheet
import java.util.Date

object ScheduleExporter extends ExcelExporter {

  def doExport(tournament: Tournament)(outputStream: OutputStream) {

    val workbook = loadWorkbook("schedule");

    val sheet = workbook.getSheetAt(0);

    sheet.getOrCreateRow(0).getOrCreateCell(0).setCellValue(tournament.name.get);

    val pools = tournament.rounds.flatMap(_.pools).sortBy(_.startTime.get);

    var i = 2;

    for (pool <- pools) {

      i = i + 1;

      val poolRow = sheet.getOrCreateRow(i);

      poolRow.getOrCreateCell(0).setCellValue(pool.poolName)

      for (fight <- pool.fights) {

        val row = sheet.getOrCreateRow(i);

        row.getOrCreateCell(1).setCellValue(fight.order.get)
        row.getOrCreateCell(2).setCellValue(new Date(fight.plannedStartTime))
        row.getOrCreateCell(3).setCellValue(fight.fighterA.foreign.get.name.get)
        row.getOrCreateCell(4).setCellValue(fight.fighterB.foreign.get.name.get)

        i = i + 1;
      }
    }

    workbook.write(outputStream);
  }

}