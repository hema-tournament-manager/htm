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
import nl.malienkolders.htm.admin.lib.exporter._
import nl.malienkolders.htm.admin.lib.Utils.PimpedParticipant
import scala.xml.Text

object ParticipantList {

  def render = {
    val ps = Participant.findAll(OrderBy(Participant.name, Ascending))

    def registerAll(register: Boolean) = {
      ps foreach { p =>
        p.isPresent(register)
        p.subscriptions foreach (_.gearChecked(register))
        p.save
      }
      S.redirectTo("/participants/list")
    }

    def createParticipant() = {
      Participant.create.externalId("new").country(Country.findAll().find(_.code2.is == "NL").get).save

      S.redirectTo("/participants/register/new")
    }

    def registerAllLink = SHtml.link("/participants/list", () => registerAll(true), <span><span class="glyphicon glyphicon-ok"></span> Register All</span>)

    def unregisterAllLink = SHtml.link("/participants/list", () => registerAll(false), <span><span class="glyphicon glyphicon-remove"></span> Unregister All</span>)

    def createParticipantLink = SHtml.link("/participants/list", createParticipant, <span><span class="glyphicon glyphicon-plus-sign"></span> New Participant</span>)

    ".downloadButton *" #> Seq(
      SHtml.link("/download/participants", () => throw new ResponseShortcutException(downloadParticipantList), Text("Participants")),
      SHtml.link("/download/clubs", () => throw new ResponseShortcutException(downloadClubsList), Text("Clubs")),
      SHtml.link("/download/details", () => throw new ResponseShortcutException(downloadDetailsList), Text("Finalist Details"))) &
      ".actionButton *" #> Seq(createParticipantLink, registerAllLink, unregisterAllLink)
  }

  def downloadParticipantList() = {
    OutputStreamResponse(ParticipantsExporter.doExport _, List("content-disposition" -> "inline; filename=\"participants.xls\""))
  }

  def downloadClubsList() = {
    OutputStreamResponse(ClubsExporter.doExport _, List("content-disposition" -> "inline; filename=\"clubs.xls\""))
  }

  def downloadDetailsList() = {
    OutputStreamResponse(DetailsExporter.doExport _, List("content-disposition" -> "inline; filename=\"details.xls\""))
  }

}