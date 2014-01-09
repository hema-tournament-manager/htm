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

object PoolsExporter extends ExcelExporter {

  def doExport(tournament: Tournament)(outputStream: OutputStream): Unit = {
    val workbook = loadWorkbook("pools");

    val sheet = workbook.getSheetAt(0);

    sheet.getOrCreateRow(0).getOrCreateCell(0).setCellValue("Pools " + tournament.name.get)

    var i = 2;

    for (pool <- tournament.poolPhase.pools) {
      i = i + 1;
      val poolRow = sheet.getOrCreateRow(i);
      poolRow.getOrCreateCell(0).setCellValue(pool.poolName)

      for (p <- pool.participants) {

        val row = sheet.getOrCreateRow(i);

        row.getOrCreateCell(1).setCellValue(p.subscription(tournament).get.fighterNumber.get)
        row.getOrCreateCell(2).setCellValue(p.name.get)
        row.getOrCreateCell(3).setCellValue(p.club.get)

        i = i + 1;
      }
    }

    workbook.write(outputStream);
  }

}