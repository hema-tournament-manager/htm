package bootstrap.liftweb

import net.liftweb._
import util._
import Helpers._
import common._
import http._
import sitemap._
import Loc._
import mapper._
import nl.malienkolders.htm.admin.model._
import nl.malienkolders.htm.admin.snippet._
import nl.malienkolders.htm.lib._
import nl.malienkolders.htm.lib.model._
import nl.malienkolders.htm.admin.snippet.TournamentView
import nl.malienkolders.htm.admin.AdminRest
import nl.malienkolders.htm.lib.RoundRobinTournament

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {
    if (!DB.jndiJdbcConnAvailable_?) {
      val vendor =
        new StandardDBVendor(Props.get("db.driver") openOr "org.h2.Driver",
          Props.get("db.url") openOr
            "jdbc:h2:htm_admin",
          Props.get("db.user") or Full("sa"), Props.get("db.password") or Full("masterkey"))

      LiftRules.unloadHooks.append(vendor.closeAllConnections_! _)

      DB.defineConnectionManager(DefaultConnectionIdentifier, vendor)
    }

    // Use Lift's Mapper ORM to populate the database
    // you don't need to use Mapper to use Lift... use
    // any ORM you want
    Schemifier.schemify(true, Schemifier.infoF _, User, Country, Score, model.Tournament, TournamentParticipants, Round, Pool, PoolParticipants, Participant, Fight, ParticipantNameMapping, Viewer, ArenaViewers, Arena)

    // where to search snippet
    LiftRules.addToPackages("nl.malienkolders.htm.admin")
    LiftRules.addToPackages("nl.malienkolders.htm.lib")

    LiftRules.liftRequest.append {
      case Req("static" :: "battle" :: "templates" :: "arenas" :: Nil, "html", _) => false
    }
    LiftRules.liftRequest.append {
      case Req("static" :: "battle" :: "templates" :: "exchangeList" :: Nil, "html", _) => false
    }
    LiftRules.liftRequest.append {
      case Req("static" :: "battle" :: "templates" :: "fight" :: Nil, "html", _) => false
    }
    LiftRules.liftRequest.append {
      case Req("static" :: "viewer" :: "templates" :: "controller" :: Nil, "html", _) => false
    }

    CountryImporter.doImport

    RoundRobinTournament.register
    SwissTournament.register
    SwissSpecialHitsTournament.register

    val entries: List[ConvertableToMenu] = (Menu.i("Home") / "index") ::
      (Menu.i("Tournaments") / "tournaments" / "list") ::
      TournamentView.menu ::
      TournamentEdit.menu ::
      TournamentAdvance.menu ::
      FightEdit.menu ::
      (Menu.i("Participants") / "participants" / "list") ::
      ParticipantRegistration.menu ::
      (Menu.i("Arenas") / "arenas" / "list") ::
      (Menu.i("Viewers") / "viewers" / "list") ::
      (Menu.i("Import") / "import") ::
      (Menu.i("Export") / "export") ::
      (Menu.i("Battle") / "battle") ::
      (Menu.i("Controller") / "viewer") ::
      Nil

    // Build SiteMap
    def sitemap = SiteMap(entries: _*)

    // def sitemapMutators = User.sitemapMutator

    // set the sitemap.  Note if you don't want access control for
    // each page, just comment this line out.
    LiftRules.setSiteMapFunc(() => sitemap)

    LiftRules.statelessDispatchTable.append(AdminRest)

    // Use jQuery 1.4
    LiftRules.jsArtifacts = net.liftweb.http.js.jquery.JQuery14Artifacts

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // What is the function to test if a user is logged in?
    LiftRules.loggedInTest = Full(() => User.loggedIn_?)

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))

    // Make a transaction span the whole HTTP request
    S.addAround(DB.buildLoanWrapper)
  }
}
