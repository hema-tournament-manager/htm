package nl.malienkolders.htm.admin.lib.exporter

import org.apache.poi.ss.usermodel._

abstract class ExcelExporter {

  implicit class PimpedRow(row: Row) {

    def getOrCreateCell(index: Int): Cell = {
      val cell = row.getCell(index)
      if (cell == null) {
        return row.createCell(index);
      } else {
        return cell;
      }
    }

  }

  implicit class PimpedSheet(sheet: Sheet) {

    def getOrCreateRow(index: Int): Row = {
      val row = sheet.getRow(index)
      if (row == null) {
        return sheet.createRow(index);
      } else {
        return row;
      }
    }

  }

  def loadTemplate(name: String) = getClass().getResourceAsStream(s"${name}_template.xls")

  def loadWorkbook(name: String) = WorkbookFactory.create(loadTemplate(name))

}