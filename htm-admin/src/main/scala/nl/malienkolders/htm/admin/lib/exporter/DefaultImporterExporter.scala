package nl.malienkolders.htm.admin.lib.exporter

import java.io.OutputStream
import nl.malienkolders.htm.lib.model.Participant
import nl.malienkolders.htm.lib.model.Tournament

object DefaultImporterExporter extends ExcelExporter {

  def doExport(out: OutputStream): Unit = {
    val workbook = loadWorkbook("default_importer")
    val sheet = workbook.getSheetAt(0)
    sheet.protectSheet(null)
    
    val tournaments = Tournament.findAll();

    for ((t, i) <- tournaments.zipWithIndex) {
      sheet.getRow(0).getOrCreateCell(6 + i).setCellValue(t.identifier.get)
    }
    
    for ((p, i) <- Participant.findAll().sortBy(_.externalId.get.toInt).zipWithIndex) {
      val row = sheet.getOrCreateRow(i + 1)
      row.getOrCreateCell(0).setCellValue(p.externalId.get.toInt)
      row.getOrCreateCell(1).setCellValue(p.name.get)
      row.getOrCreateCell(2).setCellValue(p.shortName.get)
      row.getOrCreateCell(3).setCellValue(p.club.get)
      row.getOrCreateCell(4).setCellValue(p.clubCode.get)
      row.getOrCreateCell(5).setCellValue(p.country.foreign.get.code2.get)
      
      for {
        (t, j) <- tournaments.zipWithIndex
        _ <- p.subscription(t)
      } {
        row.getOrCreateCell(6 + j).setCellValue("X")
      }
    }
    
    sheet.protectSheet("welkom")
    workbook.write(out)
  }

}