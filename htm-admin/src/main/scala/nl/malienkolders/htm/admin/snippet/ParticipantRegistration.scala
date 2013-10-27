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

  implicit class PimpedSubscription(sub: TournamentParticipants) {
    def pool = {
      val participantId = sub.participant.get
      val tournament = sub.tournament.obj.get

      val poolName = for {
        firstRound <- tournament.rounds.find(_.order.get == 1)
        pool <- firstRound.pools.find(_.participants.exists(_.id.get == participantId))
      } yield { pool.poolName }

      poolName.getOrElse("?")
    }
  }

  def render = {
    val p = Participant.find(By(Participant.externalId, ParticipantRegistration.loc.currentValue.map(_.param).get)).get

    def process() = {
      p.save
      S.redirectTo("/participants/list")
    }

    ".photo [src]" #> s"/photo/${p.externalId.get}/r" &
      "name=name" #> p.name.toForm &
      "name=shortName" #> p.shortName.toForm &
      "name=club" #> p.club.toForm &
      "name=clubCode" #> p.clubCode.toForm &
      "name=externalId" #> p.externalId.toForm &
      "name=present" #> p.isPresent.toForm &
      ".subscription" #> p.subscriptions.sortBy(!_.primary.get).map(sub =>
        "a" #> <a name={ "tournament" + sub.tournament.obj.get.id.get.toString }></a> &
          ".tournament *" #> sub.tournament.obj.get.name.get &
          ".pool *" #> sub.pool &
          ".fighterNumber *" #> sub.fighterNumber.asHtml &
          ".experience" #> sub.experience.toForm &
          ".gear" #> sub.gearChecked.toForm) &
      "name=save" #> SHtml.submit("Save", process, "class" -> "btn btn-primary")

  }

}