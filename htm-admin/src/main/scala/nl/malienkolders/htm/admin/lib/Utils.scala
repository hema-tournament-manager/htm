package nl.malienkolders.htm.admin.lib

import nl.malienkolders.htm.lib.model.Participant
import java.io.File
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.mapper._
import org.apache.commons.io.FileUtils
import java.text.SimpleDateFormat
import java.util.Date
import com.github.nscala_time.time.Imports._

object Utils {

  object Constants {
    val ROUND_NAME_FINAL = "Final"
    val ROUND_NAME_THIRD_PLACE = "3rd Place"
  }

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

  val yyyymmdd = DateTimeFormat.forPattern("yyyy-MM-dd").withZone(DateTimeZone.UTC)
  val hhmm = DateTimeFormat.forPattern("HH:mm").withZone(DateTimeZone.UTC)

  implicit class TimeRenderHelper(time: Long) {
    def hhmm = LocalTime.fromMillisOfDay(time).toString(Utils.hhmm)
    def as(format: String) = new SimpleDateFormat(format).format(new Date(time))
  }

  implicit class DateTimeParserHelper(time: String) {
    def yyyymmdd = Utils.yyyymmdd.parseLocalDate(time).toDate().getTime()
    def hhmm = Utils.hhmm.parseLocalTime(time).getMillisOfDay()
  }

}