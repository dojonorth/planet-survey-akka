package uk.co.bbc.dojo.awaymission

import akka.actor.ActorSystem
import akka.util.Timeout
import uk.co.bbc.dojo.awaymission.actors.StarshipCommand
import uk.co.bbc.dojo.awaymission.actors.StarshipCommand.SeekOutNewLifeAndNewCivilisations
import uk.co.bbc.dojo.awaymission.locations.Planet
import akka.pattern.ask //Needed for the ask pattern.

import scala.concurrent.Await
import scala.concurrent.duration._

class AwayMission {
  def surveyPlanets(planetsToSurvey: List[Planet]): Int = {
    val akkaSystem = ??? // Create the akka system here,

    val starshipCommand = ??? // Create starship command here.

    implicit val timeout = Timeout(Duration.create(20, SECONDS))
    val futureResponse = ??? // Message starship command with a SeekOutNewLifeAndNewCivilisations(planetsToSurvey) message. Use the '?' operator (should be the only place you use it).

    val inhabitedPlanets = Await.result(futureResponse, timeout.duration).asInstanceOf[Int]
    inhabitedPlanets
  }
}
