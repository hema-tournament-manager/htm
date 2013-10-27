package nl.malienkolders.htm.admin.lib

import nl.malienkolders.htm.lib.model.Participant
import java.io.File

object Utils {

  implicit class PimpedParticipant(p: Participant) {
    private def zeroPad(id: String) = ("0" * (4 - id.length)) + id

    def avatarOriginalFile = new File(s"Avatars/${zeroPad(p.externalId.get)}.jpg")

    def avatarLeftFile = new File(s"Avatars/Generated/${zeroPad(p.externalId.get)}_default_l.jpg")

    def avatarRightFile = new File(s"Avatars/Generated/${zeroPad(p.externalId.get)}_default_r.jpg")

    def hasAvatar = avatarOriginalFile.exists
  }

}