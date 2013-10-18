package lib

import org.specs2.mutable._
import java.util.zip.ZipOutputStream
import java.io.FileOutputStream
import java.io.File
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.io.ByteArrayInputStream

class UnzipperSpec extends Specification {

  "Unzipper" should {
    "unzip files (really!)" in {
      val testOutput = new File("test.txt")
      testOutput.delete()
      val buffer = new ByteArrayOutputStream()
      val out = new ZipOutputStream(buffer)
      out.putNextEntry(new ZipEntry("test.txt"))
      out.write("TESTING...".getBytes())
      out.closeEntry()
      out.close()
      Unzipper.unzip(new File("."), new ZipInputStream(new ByteArrayInputStream(buffer.toByteArray())))

      testOutput.exists() must beTrue
    }

    "unzip nested files" in {
      val testOutput = new File("testDir/testFile.txt")
      if (testOutput.exists())
        testOutput.delete()
      val buffer = new ByteArrayOutputStream()
      val out = new ZipOutputStream(buffer)
      out.putNextEntry(new ZipEntry("testDir/testFile.txt"))
      out.write("TESTING...".getBytes())
      out.closeEntry()
      out.close()
      Unzipper.unzip(new File("."), new ZipInputStream(new ByteArrayInputStream(buffer.toByteArray())))

      testOutput.exists() must beTrue
    }

  }

}
