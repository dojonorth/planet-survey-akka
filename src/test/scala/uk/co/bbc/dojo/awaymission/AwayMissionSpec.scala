package uk.co.bbc.dojo.awaymission

import org.scalatest.{Matchers, FunSpec}
import uk.co.bbc.dojo.awaymission.locations.{AbandonedClangerOutpost, ClangerPrime, ScannablePlanet}

class AwayMissionSpec extends FunSpec with Matchers {
    describe("To bravely go where no person has gone before") {
      it("Part 1 - Starship Command should respond to our message") {
        val awayMission = new AwayMission
        val numberOfPlanetsWithLife = awayMission.surveyPlanets(List())

        numberOfPlanetsWithLife should be(0)
      }

      it("Part 2 - the En Prise should survey a single live planet") {
        val planetWithLife = ScannablePlanet("Skaro", true)

        val awayMission = new AwayMission
        val numberOfPlanetsWithLife = awayMission.surveyPlanets(List(planetWithLife))

        numberOfPlanetsWithLife should be(1)
      }

      it("Part 3 - The En Prise should survey multiple planets") {
        val planetsWithLife = List(ScannablePlanet("Dinosaur", true), ScannablePlanet("Carpathia", true))
        val planetsWithoutLife = List(ScannablePlanet("Rimmerworld", false))

        val awayMission = new AwayMission
        val numberOfPlanetsWithLife = awayMission.surveyPlanets(planetsWithLife ++ planetsWithoutLife)

        numberOfPlanetsWithLife should be(planetsWithLife.length)
      }

      // Warning, the default behaviour is that the bad message that caused us to explore ClangerPrime will be thrown away,
      // the En Prise will be recreated and will process the remaining messages. However, Starship Command will never
      // receive a response for Clanger Prime and so will eventually timeout.
      it("Part 4 - The En Prise being destroyed will not stop the remaining planets being scanned.") {
        val planetsToScan = List(ScannablePlanet("Zygor", true), ScannablePlanet("Tripod", true), ClangerPrime)

        val awayMission = new AwayMission
        val numberOfPlanetsWithLife = awayMission.surveyPlanets(planetsToScan)

        numberOfPlanetsWithLife should be(planetsToScan.length)
      }

      it("Part 5 - The En prise being destroyed will not cause command to assume alien life. Instead a more capable ship will be dispatched to make sure.") {
        val planetsToScan = List(AbandonedClangerOutpost, ClangerPrime, ScannablePlanet("Tripod", true))

        val awayMission = new AwayMission
        val numberOfPlanetsWithLife = awayMission.surveyPlanets(planetsToScan)

        numberOfPlanetsWithLife should be(2)
      }

      it("Part 6 - The fleet will efficiently scan a range of planets") {
        val planetsToScan = List(ScannablePlanet("Dity-365", true, 1200), ScannablePlanet("Yol-1", false, 1500), ScannablePlanet("Doc-12", false, 3500),
                                 ScannablePlanet("LBA-40", false, 500), ScannablePlanet("Pitss-8n", true, 2000), ScannablePlanet("Seven-11", true, 4200),
                                 ScannablePlanet("Cimpn-439", false, 200), ScannablePlanet("Majestic-12", true, 1100), ScannablePlanet("Omega-5", false, 2900),
                                 ScannablePlanet("Omega-5", false, 2300))

        val awayMission = new AwayMission

        val startTime = System.currentTimeMillis()
        val numberOfPlanetsWithLife = awayMission.surveyPlanets(planetsToScan)
        val endTime = System.currentTimeMillis()

        numberOfPlanetsWithLife should be(4)

        val totalTime = (endTime - startTime) / 1000.0
        totalTime should be(7.0 +- 0.5) //If you have a REALLY slow computer, you may need to increase the variance slightly.
      }
    }
}
