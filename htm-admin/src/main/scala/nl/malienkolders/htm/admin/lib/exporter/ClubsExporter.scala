package nl.malienkolders.htm.admin.lib.exporter

import nl.malienkolders.htm.lib.model._
import java.io.OutputStream

object ClubsExporter extends ExcelExporter {

  def doExport(outputStream: OutputStream): Unit = {
    val workbook = loadWorkbook("clubs");

    val sheet = workbook.getSheetAt(0);

    var i = 2;

    val allClubs = Participant.findAll.map(p => p.clubCode.get -> p.club.get).toSet
    val relevantClubs = allClubs.filterNot(_._1.contains("/")).filterNot(_._1.isEmpty())

    for ((clubCode, clubName) <- relevantClubs.toList.sortBy(_._1)) {
      i = i + 1;
      val row = sheet.getOrCreateRow(i);
      row.getOrCreateCell(0).setCellValue(clubCode)
      row.getOrCreateCell(1).setCellValue(clubName)
    }

    workbook.write(outputStream);
  }

}