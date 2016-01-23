package uk.co.bbc.dojo.awaymissionsolution

import _root_.akka.pattern.ask
import _root_.akka.actor.ActorSystem
import _root_.akka.util.Timeout
import uk.co.bbc.dojo.awaymissionsolution.akka.actors.StarshipCommandSolution
import uk.co.bbc.dojo.awaymissionsolution.akka.actors.StarshipCommandSolution.SeekOutNewLifeAndNewCivilisationsSolution
import uk.co.bbc.dojo.awaymissionsolution.locations.PlanetSolutionSolution

import scala.concurrent.Await
import scala.concurrent.duration._

class AwayMissionSolution {
  /** Returns the number of planets with life on them */
  def surveyPlanets(planetsToSurvey: List[PlanetSolutionSolution]): Int = {
    val akkaSystem = ActorSystem("The-Corperation")

    val starshipCommand = akkaSystem.actorOf(StarshipCommandSolution(), name = "Admiral-Reith")

    implicit val timeout = Timeout(Duration.create(20, SECONDS))
    val futureResponse = starshipCommand ? SeekOutNewLifeAndNewCivilisationsSolution(planetsToSurvey)

    val occupiedPlanets = Await.result(futureResponse, timeout.duration).asInstanceOf[Int]
    occupiedPlanets
  }
}
