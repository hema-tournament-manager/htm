package nl.htm.importer.heffac

import org.specs2._
import nl.htm.importer._
import java.io.File
import java.io.FileInputStream

class HeffacImporterSpec extends mutable.Specification {
  
  "HeffacImporter" should {
    "import countries correctly" in {
      HeffacImporter.doImport(InputStreamSettings(new FileInputStream("/home/jogchem/heffaf_inschrijvingen.xlsx"))).participants.groupBy(_.country).size must beEqualTo(2)
    }
  }

}