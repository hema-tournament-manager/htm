package nl.malienkolders.htm.admin
package comet

import nl.malienkolders.htm.lib.model._
import net.liftweb._
import common._
import http._
import util._
import Helpers._
import js._
import JsCmds._
import scala.xml.Text
import mapper._

object FightPlanner extends CometActor {

  var fights = Fight.findAll(OrderBy(Fight.order, Ascending))

  def render = "a" #> "Test"

}