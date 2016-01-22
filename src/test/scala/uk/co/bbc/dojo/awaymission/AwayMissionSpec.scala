package uk.co.bbc.dojo.awaymission

import org.scalatest.{FunSpec, Matchers}
import uk.co.bbc.dojo.awaymission.locations.{ClangerPrime, ScannablePlanet, Planet}

class AwayMissionSpec extends FunSpec with Matchers {
    describe("To bravely go where no person has gone before") {
      ignore("the En Prise should survey a single planet") {
        val planetWithLife = ScannablePlanet("Gallifrey", true)

        val awayMission = new AwayMission
        val numberOfPlanetsWithLife = awayMission.surveyPlanets(List(planetWithLife))

        numberOfPlanetsWithLife should be(1)
      }

      ignore("the En Prise should survey multiple planets") {
        val planetsWithLife = List(ScannablePlanet("Dinosaur", true), ScannablePlanet("Carpathia", true))
        val planetsWithoutLife = List(ScannablePlanet("Rimmerworld", false))

        val awayMission = new AwayMission
        val numberOfPlanetsWithLife = awayMission.surveyPlanets(planetsWithLife ++ planetsWithoutLife)

        numberOfPlanetsWithLife should be(planetsWithLife.length)
      }

      // Warning, the default behaviour is that the bad message that caused us to explore ClangerPrime will be thrown away,
      // the En Prise will be recreated and will process the remaining messages. However, Starship Command will never
      // receive a response for Clanger Prime and so will eventually timeout.
      ignore("the En Prise being destroyed will not stop the remaining planets being scanned.") {
        val planetsToScan = List(ScannablePlanet("Zygor", true), ClangerPrime(), ScannablePlanet("Tripod", true))

        val awayMission = new AwayMission
        val numberOfPlanetsWithLife = awayMission.surveyPlanets(planetsToScan)

        // Starship Command assumes that the planet was inhabited if it destroyed the En Prise.
        numberOfPlanetsWithLife should be(planetsToScan.length)
      }
    }
}
