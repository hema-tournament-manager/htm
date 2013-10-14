package nl.htm.importer.heffac

import org.specs2.mutable._
import nl.htm.importer._

class HeffacImporterSpec extends Specification {

  "HeffacImporter" should {
    "import countries correctly" in {
      HeffacImporter.doImport(new EmptySettings).participants.groupBy(_.country).size must beEqualTo(2)
    }
  }

}