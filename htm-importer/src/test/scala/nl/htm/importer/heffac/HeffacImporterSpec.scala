package nl.htm.importer.heffac

import org.specs2.mutable._
import nl.htm.importer._
import java.io.File
import java.io.FileInputStream

class HeffacImporterSpec extends Specification {

  "HeffacImporter" should {
    "import countries correctly" in {
      HeffacImporter.doImport(HeffacSettings(new FileInputStream("/home/jogchem/heffaf_inschrijvingen.xlsx"))).participants.groupBy(_.country).size must beEqualTo(2)
    }
  }

}