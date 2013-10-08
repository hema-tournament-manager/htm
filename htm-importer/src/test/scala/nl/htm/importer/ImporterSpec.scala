package nl.htm.importer

import org.specs2.mutable._

class ImporterSpec extends Specification {

  "DummyImporter" should {
    "produce 7 people" in {
      val data = DummyImporter.doImport()
      data.participants.length must beEqualTo(7)
    }

    "produce 2 tournaments" in {
      val data = DummyImporter.doImport()
      data.tournaments.length must beEqualTo(2)
    }
  }

}
