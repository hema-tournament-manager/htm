package nl.malienkolders.htm.admin.lib

import java.io.File
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream
import java.util.zip.ZipEntry

object PhotoImporterBackend {

  def doImport(file: Array[Byte]) = {

    val stream = new ByteArrayInputStream(file);
    val rootzip = new ZipInputStream(stream)

    import collection.JavaConverters._

    def handle(in: ZipInputStream): Unit = in.getNextEntry() match {
      case e: ZipEntry =>
        println(e.getName())
        handle(in)
      case _ => in.close()
    }

    handle(rootzip)
    /*
	var entry : ZipEntry = null;
	while((entry = rootzip.getNextEntry()) != null)
	{
		//val entry : ZipEntry = rootzip.getNextEntry();
		println("Processing entry %s with length %d" format(entry.getName(), entry.getSize()))
		/*val bytes = new Array[Byte](entry.getSize().toInt)
		
		
		rootzip.read(bytes, 0, entry.getSize().toInt)
		
		println("Name: %s" format entry.getName())*/
		
		//drootzip.closeEntry()
	}*/
  }
}