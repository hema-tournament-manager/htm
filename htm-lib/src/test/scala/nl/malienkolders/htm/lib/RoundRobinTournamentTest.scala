package nl.malienkolders.htm.lib

import org.specs2.mutable._

class RoundRobinTournamentTest extends Specification {
  "RoundRobinTournament" should {
    "rotate 0" in {
      RoundRobinTournament.rotate(List(2), List(3, 4), 0) must beEqualTo((List(2), List(3, 4)))
    }
    "rotate 1" in {
      RoundRobinTournament.rotate(List(2), List(3, 4), 1) must beEqualTo((List(4), List(2, 3)))
    }
    "rotate 2" in {
      RoundRobinTournament.rotate(List(2), List(3, 4), 2) must beEqualTo((List(3), List(4, 2)))
    }
    "create top row for even numbers" in {
      RoundRobinTournament.topRowForCount(4) must beEqualTo(List(2))
    }
    "create bottom row for even numbers" in {
      RoundRobinTournament.bottomRowForCount(4) must beEqualTo(List(3, 4))
    }
    "create top row for odd numbers" in {
      RoundRobinTournament.topRowForCount(5) must beEqualTo(List(2, 3))
    }
    "create bottom row for odd numbers" in {
      RoundRobinTournament.bottomRowForCount(5) must beEqualTo(List(4, 5, -1))
    }
    "plan even" in {
      RoundRobinTournament.roundRobinPairing(4, 0) must beEqualTo(List((1, 4), (2, 3)))
    }
    "plan odd" in {
      RoundRobinTournament.roundRobinPairing(5, 0) must beEqualTo(List((1, -1), (2, 5), (3, 4)))
    }
  }
}