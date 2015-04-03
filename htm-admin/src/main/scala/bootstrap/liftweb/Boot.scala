/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/

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
import nl.malienkolders.htm.admin.AdminRest
import nl.malienkolders.htm.lib.rulesets.socal2014._
import nl.malienkolders.htm.admin.comet.RefreshServer
import nl.malienkolders.htm.lib.util.Helpers
import java.net.MulticastSocket
import java.net.DatagramPacket
import net.liftweb.util.Schedule
import nl.malienkolders.htm.admin.lib.importer.ResourceBundleImporter

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
      FreeStyleFightScore,
      EliminationFightScore,
      model.Tournament,
      TournamentParticipant,
      PoolPhase,
      FreeStylePhase,
      EliminationPhase,
      Pool,
      PoolParticipants,
      Participant,
      PoolFight,
      FreeStyleFight,
      EliminationFight,
      ParticipantNameMapping,
      Viewer,
      ArenaViewers,
      Arena,
      ScheduledPoolFight,
      ScheduledFreeStyleFight,
      ScheduledEliminationFight,
      Image,
      ScaledImage,
      Event,
      Day,
      ArenaTimeSlot)

    CountryImporter.doImport

    LongswordRuleset.register(true)
    SwordBucklerRuleset.register()
    nl.malienkolders.htm.lib.rulesets.mexico2014.EmagRuleset.registerAll()
    nl.malienkolders.htm.lib.rulesets.kriegesschule2014.KriegesSchuleRuleset.registerAll()
    nl.malienkolders.htm.lib.rulesets.bergen2014.BergenOpenRuleset.registerAll()
    nl.malienkolders.htm.lib.rulesets.fightcamp2014.FightcampRuleset.registerAll

    // LiftRules.statelessDispatch.append(AdminRest)
    LiftRules.dispatch.append(AdminRest)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))

    // allow huge uploads for the photo import
    LiftRules.maxMimeSize = 1024L * 1024L * 1024L
    LiftRules.maxMimeFileSize = 1024L * 1024L * 1024L

    // Make a transaction span the whole HTTP request
    S.addAround(DB.buildLoanWrapper)

    //  RefreshServer;

    Helpers.openUrlFromSystemProperty("htm.admin.url")

    ResourceBundleImporter.run()
  }
}
