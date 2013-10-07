package nl.malienkolders.htm.lib

import nl.malienkolders.htm.lib.model._

abstract class Tournament {

  def planning(round: Round): List[Pool]

}