package uk.co.bbc.dojo.awaymission

import _root_.akka.pattern.ask
import _root_.akka.actor.ActorSystem
import _root_.akka.util.Timeout
import uk.co.bbc.dojo.awaymission.akka.actors.StarshipCommand
import uk.co.bbc.dojo.awaymission.akka.actors.StarshipCommand.SeekOutNewLifeAndNewCivilisations
import uk.co.bbc.dojo.awaymission.locations.Planet

import scala.concurrent.Await
import scala.concurrent.duration._

class AwayMission {
  /* Returns the number of planets with life on them */
  //TODO: Intially return 0.
  def surveyPlanets(planetsToSurvey: List[Planet]): Int = {
    val akkaSystem = ActorSystem("The-Corperation")

    val starshipCommand = akkaSystem.actorOf(StarshipCommand(), name = "Admiral-Reith")

    implicit val timeout = Timeout(Duration.create(20, SECONDS))
    val futureResponse = starshipCommand ? SeekOutNewLifeAndNewCivilisations(planetsToSurvey)

    val occupiedPlanets = Await.result(futureResponse, timeout.duration).asInstanceOf[Int]
    occupiedPlanets
  }
}
