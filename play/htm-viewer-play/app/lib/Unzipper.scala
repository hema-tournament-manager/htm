package lib

import java.io.File
import java.util.zip.ZipInputStream
import java.util.zip.ZipEntry
import org.apache.commons.io.IOUtils
import java.io.FileOutputStream
import play.api.Logger
import java.io.FileInputStream

object Unzipper {

  lazy val log = Logger("Unzipper")

  def unzip(targetDir: File, file: File): Unit = {
    unzip(targetDir, new ZipInputStream(new FileInputStream(file)))
  }

  def unzip(targetDir: File, in: ZipInputStream): Unit = in.getNextEntry() match {
    case entry: ZipEntry =>
      println("entry: " + entry.getName())
      log.info("Unzipping " + entry.getName())
      val target = new File(targetDir, entry.getName())
      target.getParentFile().mkdirs()
      println("File: " + target.getName())
      val out = new FileOutputStream(target)
      try {
        IOUtils.copy(in, out)
      } finally {
        out.close()
      }
      unzip(targetDir, in)
    case _ => in.close()
  }

}