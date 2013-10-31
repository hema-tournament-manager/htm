package nl.malienkolders.htm.admin.lib

import nl.malienkolders.htm.lib.model.Participant
import java.io.File
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.mapper._
import org.apache.commons.io.FileUtils
import java.text.SimpleDateFormat
import java.util.Date

object Utils {

  implicit class PimpedParticipant(p: Participant) {
    private def zeroPad(id: String) = ("0" * (4 - id.length)) + id

    def avatarOriginalFile = new File(s"Avatars/${zeroPad(p.externalId.get)}.jpg")

    def avatarLeftFile = new File(s"Avatars/Generated/${zeroPad(p.externalId.get)}_default_l.jpg")

    def avatarRightFile = new File(s"Avatars/Generated/${zeroPad(p.externalId.get)}_default_r.jpg")

    def hasAvatar = avatarOriginalFile.exists
  }

  def photo(pariticipantExternalId: String, side: String): Box[LiftResponse] = {
    for {
      p <- Participant.find(By(Participant.externalId, pariticipantExternalId))
    } yield {
      val bytes = FileUtils.readFileToByteArray(if (side == "l") p.avatarLeftFile else p.avatarRightFile)
      InMemoryResponse(bytes, ("Content-Type" -> "image/jpeg") :: Nil, Nil, 200)
    }

  }

  implicit class TimeRenderHelper(time: Long) {
    def as(format: String) = new SimpleDateFormat(format).format(new Date(time))
  }

}