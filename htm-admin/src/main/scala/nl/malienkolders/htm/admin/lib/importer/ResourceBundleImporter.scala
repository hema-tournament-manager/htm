package nl.malienkolders.htm.admin.lib.importer

import java.io.File
import nl.malienkolders.htm.lib.model.Resolution
import java.io.FilenameFilter
import nl.malienkolders.htm.admin.snippet.ImageList
import java.io.FileInputStream
import net.liftweb.common.Loggable

object ResourceBundleImporter extends Loggable {

  def run(): Unit = {
    val root = new File("resourcebundle")
    if (root.exists) {
      for (reso <- Resolution.supported) {
        val resoDir = new File(root, reso.toString)
        if (resoDir.exists) {
          for (file <- resoDir.listFiles(new FilenameFilter { override def accept(dir: File, name: String): Boolean = name.toLowerCase().endsWith(".png") })) {
            val in = new FileInputStream(file)
            ImageList.importImage(file.getName.dropRight(4), reso, file.getName(), "image/png", in)
            in.close
          }
        }
      }
    }
  }

}