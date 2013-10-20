package nl.malienkolders.htm.admin
package snippet

import net.liftweb._
import common._
import http._
import mapper._
import sitemap._
import util.Helpers._
import nl.malienkolders.htm.lib.model._

object ParticipantSwap {

  val menu = Menu.param[ParamInfo]("Swap", "Swap", s => Full(ParamInfo(s)),
    pi => pi.param) / "participants" / "swap"
  lazy val loc = menu.toLoc

  def render = {
    val p = Participant.find(By(Participant.externalId, ParticipantSwap.loc.currentValue.map(_.param).get)).get
    val others = Participant.findAll.filter(_.id.is != p.id.is).toList.sortBy(_.externalId.is.toLong)
    var otherId: Long = -1

    def process() = {
      val externalId = p.externalId.is
      val name = p.name.is
      val shortName = p.shortName.is
      val club = p.club.is
      val clubCode = p.clubCode.is
      val country = p.country.is

      Participant.findByKey(otherId) match {
        case Full(other) =>
          p.
            externalId(other.externalId.is).
            name(other.name.is).
            shortName(other.shortName.is).
            club(other.club.is).
            clubCode(other.clubCode.is).
            country(other.country.is)

          other.
            externalId(externalId).
            name(name).
            shortName(shortName).
            club(club).
            clubCode(clubCode).
            country(country)

          p.save
          other.save

          S.notice("Swapped " + p.externalId.is + " and " + other.externalId.is)
        case _ => S.notice("Fail")
      }

      S.redirectTo("/participants/list")
    }

    ".name *" #> p.name.is &
      ".id *" #> p.externalId.is &
      ".other" #> SHtml.select(others.map(o => o.id.get.toString -> (o.externalId.is + " " + o.name.is)), Empty, input => otherId = input.toLong) &
      "name=save" #> SHtml.submit("Save", () => process)

  }

}