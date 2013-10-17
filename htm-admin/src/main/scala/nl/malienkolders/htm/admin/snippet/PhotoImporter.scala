package nl.malienkolders.htm.admin.snippet
import net.liftweb._
import http._
import util.Helpers._
import scala.xml.NodeSeq
import nl.malienkolders.htm.lib.model._
import nl.malienkolders.htm.admin.lib.ParticipantImporter
import net.liftweb.common.Full
import net.liftweb.http.js.JsCmd
import net.liftweb.common.Box
import net.liftweb.common.Empty
import net.liftweb.common.Loggable
import nl.malienkolders.htm.admin.lib.PhotoImporterBackend

object PhotoImporter extends Loggable {

	def render = {
		var clear = false
		var clearTournaments = false

		def process() {}
		
		var upload : Box[FileParamHolder] = Empty

	    def processForm() = upload match {
	      case Full(FileParamHolder(_, "application/zip", fileName, file)) =>
	        println("%s of type %s is %d bytes long" format (
	         fileName, "application/zip", file.length) )
	        PhotoImporterBackend.doImport(file)
	
	      case Full(FileParamHolder(_, mime, _, _)) => logger.error("Invalid mime-type friendo")
	      case _ => logger.warn("No file?")
	    }
	
	    "#file" #> SHtml.fileUpload(f => upload = Full(f)) &
	      "type=submit" #> SHtml.onSubmitUnit(processForm)
	}

}