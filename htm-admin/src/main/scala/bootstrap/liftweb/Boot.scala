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
import nl.malienkolders.htm.lib.rulesets.mexico2014._
import nl.malienkolders.htm.lib.rulesets.mexico2014.DefaultPoolPhaseRuleset
import nl.malienkolders.htm.lib.rulesets.mexico2014.DefaultEliminationRuleset
import nl.malienkolders.htm.lib.rulesets.mexico2014.DefaultFinalsRuleset
import nl.malienkolders.htm.lib.rulesets.mexico2014.AlbionPoolPhaseRuleset
import nl.malienkolders.htm.lib.rulesets.mexico2014.AlbionEliminationRuleset
import nl.malienkolders.htm.lib.rulesets.mexico2014.AlbionFinalsRuleset
import nl.malienkolders.htm.admin.comet.RefreshServer

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
    Schemifier.schemify(true, Schemifier.infoF _,
      User,
      Country,
      PoolFightScore,
      EliminationFightScore,
      model.Tournament,
      TournamentParticipant,
      PoolPhase,
      EliminationPhase,
      Pool,
      PoolParticipants,
      Participant,
      PoolFight,
      EliminationFight,
      ParticipantNameMapping,
      Viewer,
      ArenaViewers,
      Arena,
      ScheduledPoolFight,
      ScheduledEliminationFight,
      Image,
      ScaledImage,
      Event,
      Day,
      ArenaTimeSlot)

    // where to search snippet
    LiftRules.addToPackages("nl.malienkolders.htm.admin")
    LiftRules.addToPackages("nl.malienkolders.htm.lib")

    LiftRules.liftRequest.append {
      case Req("static" :: "battle" :: "templates" :: _ :: Nil, "html", _) => false
      case Req("static" :: "viewer" :: "templates" :: _ :: Nil, "html", _) => false
    }

    LiftRules.dispatch.append {
      case Req("image" :: resolution :: name :: Nil, _, _) => (() => ImageList.image(resolution, name))
      case Req("photo" :: pariticipantExternalId :: side :: Nil, _, _) if Set("l", "r").contains(side) =>
        (() => nl.malienkolders.htm.admin.lib.Utils.photo(pariticipantExternalId, side))
    }

    CountryImporter.doImport

    DefaultPoolPhaseRuleset.register(true)
    DefaultEliminationRuleset.register()
    DefaultFinalsRuleset.register()
    AlbionPoolPhaseRuleset.register()
    AlbionEliminationRuleset.register()
    AlbionFinalsRuleset.register()

    val entries: List[ConvertableToMenu] = (Menu.i("Event") / "index") ::
      (Menu.i("Tournaments") / "tournaments" / "list" submenus (
        TournamentView.menu,
        TournamentEdit.menu)) ::
        FightEdit.menu ::
        FightPickFighter.menu ::
        (Menu.i("Participants") / "participants" / "list") ::
        ParticipantRegistration.menu ::
        (Menu.i("Schedule") / "schedule") ::
        (Menu.i("Viewers") / "viewers" / "list") ::
        (Menu.i("Images") / "images" / "list") ::
        (Menu.i("Import") / "import") ::
        (Menu.i("Export") / "export") ::
        (Menu.i("Battle") / "battle") ::
        (Menu.i("Controller") / "viewer") ::
        RulesetModal.menu ::
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

    // allow huge uploads for the photo import
    LiftRules.maxMimeSize = 1024L * 1024L * 1024L
    LiftRules.maxMimeFileSize = 1024L * 1024L * 1024L

    // Make a transaction span the whole HTTP request
    S.addAround(DB.buildLoanWrapper)
    
  //  RefreshServer;
  }
}
