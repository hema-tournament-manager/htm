package nl.malienkolders.htm.admin.lib.exporter

import java.io.OutputStream
import nl.malienkolders.htm.lib.model.Participant

object ParticipantsExporter extends ExcelExporter {

  def doExport(out: OutputStream): Unit = {
    val workbook = loadWorkbook("participants")
    val sheet = workbook.getSheetAt(0)
    for ((p, i) <- Participant.findAll().sortBy(_.externalId.get.toInt).zipWithIndex) {
      val row = sheet.getOrCreateRow(i + 1)
      row.getOrCreateCell(0).setCellValue(p.externalId.get)
      row.getOrCreateCell(1).setCellValue(p.name.get)
      row.getOrCreateCell(2).setCellValue(p.club.get)
    }
    workbook.write(out)
  }

}