package nl.malienkolders.htm.admin
package snippet

import net.liftweb._
import common._
import http._
import mapper._
import sitemap._
import util.Helpers._
import nl.malienkolders.htm.lib.model._

object ParticipantRegistration {

  val menu = Menu.param[ParamInfo]("Registration", "Registration", s => Full(ParamInfo(s)),
    pi => pi.param) / "participants" / "register"
  lazy val loc = menu.toLoc

  def render = {
    val p = Participant.find(By(Participant.externalId, ParticipantRegistration.loc.currentValue.map(_.param).get)).get

    def process() = {
      p.save
      S.redirectTo("/participants/list")
    }

    "name=name" #> p.name.toForm &
      "name=shortName" #> p.shortName.toForm &
      "name=club" #> p.club.toForm &
      "name=clubCode" #> p.clubCode.toForm &
      "name=externalId" #> p.externalId.toForm &
      "name=present" #> p.isPresent.toForm &
      "name=equipmentCheck" #> p.isEquipmentChecked.toForm &
      "name=question1" #> p.isRankingCheck1.toForm &
      "name=question2" #> p.isRankingCheck2.toForm &
      "name=question3" #> p.isRankingCheck3.toForm &
      "name=save" #> SHtml.submit("Save", process)

  }

}