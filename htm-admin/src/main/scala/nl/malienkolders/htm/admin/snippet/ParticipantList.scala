package nl.malienkolders.htm.admin
package snippet

import nl.malienkolders.htm.lib.model._
import net.liftweb._
import common._
import util._
import Helpers._
import http._
import mapper._
import js._
import JsCmds._

object ParticipantList {

  def render = {
    val orderField: MappedField[_, Participant] = Participant.name
    val ps = Participant.findAll(OrderBy(orderField, Ascending))
    val cs = Country.findAll.map(c => c -> c.name.is)
    var selectedParticipant: Box[Participant] = Empty

    def changeCountry(p: Participant, c: Country) = {
      p.country(c).save
      var cmd = "$('#flag" + p.id.is + "').attr('title', '" + c.name.is + "'); $('#flag" + p.id.is + "').attr('src', '/images/flags/" + (if (c.hasFlag) c.code2.toLowerCase() else "unknown") + ".png'); $('#flag" + p.id.is + "');"
      if (c.hasViewerFlag.is)
        cmd += "$('#flag" + p.id.is + "').addClass('viewerFlagAvailable');"
      else
        cmd += "$('#flag" + p.id.is + "').removeClass('viewerFlagAvailable');"
      Run(cmd)
    }
    
    def registerAll() = {
      ps foreach (_.isPresent(true).isEquipmentChecked(true).save)
      S.redirectTo("/participants/list")
    }

    "#countrySelect *" #> SHtml.ajaxSelectObj(cs, Empty, { c: Country =>
      val cmd = selectedParticipant.map(p => changeCountry(p, c)) openOr (Noop)
      selectedParticipant = Empty
      cmd & Run("$('#countrySelect').hide();")
    }, "id" -> "countrySelectDropdown") &
      ".participant" #> (ps.map { p =>
        val c = p.country.obj.get
        ".participant [class]" #> (if (p.isPresent.is) "present" else "not-present") &
          ".id *" #> p.externalId.is &
          ".name *" #> p.name.is &
          ".shortName *" #> p.shortName.is &
          ".club *" #> p.club.is &
          ".clubCode *" #> p.clubCode.is &
          ".flag" #> (
            "img [src]" #> (if (c.hasFlag.is) "/images/flags/" + c.code2.toLowerCase() + ".png" else "/images/flags/unknown.png") &
            "img [class]" #> (if (c.hasViewerFlag.is) "viewerFlagAvailable" else "") &
            "img [id]" #> ("flag" + p.id.is) &
            "img [title]" #> ("%s (click to change)" format c.name.is) &
            "img [onclick]" #> SHtml.ajaxInvoke { () =>
              selectedParticipant = Full(p)
              Run("document.getElementById('countrySelectDropdown').selectedIndex = 0;$('#countrySelect').show();$('#countrySelect').offset($('#flag" + p.id.is + "').offset());") &
                Focus("countrySelectDropdown")

            }) &
            ".actions *" #> <a href={ "/participants/register/" + p.externalId.is }>register</a>
      }) &
      ".totals" #> (
          ".people *" #> ps.size &
          ".countries *" #> ps.groupBy(_.country.is).size &
          ".clubs *" #> ps.groupBy(_.clubCode.is).size &
          ".actions *" #> SHtml.submit("register all", registerAll, "class" -> "btn btn-default")
       )
  }

}