package uk.co.bbc.dojo.awaymission

import akka.pattern.ask
import akka.actor.ActorSystem
import akka.util.Timeout
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import uk.co.bbc.dojo.awaymission.actors.StarshipCommand
import uk.co.bbc.dojo.awaymission.actors.StarshipCommand.SeekOutNewLifeAndNewCivilisations
import uk.co.bbc.dojo.awaymission.locations.Planet

import scala.concurrent.Await
import scala.concurrent.duration._

class AwayMission {
  def surveyPlanets(planetsToSurvey: List[Planet]): Int = {
    val akkaSystem = ActorSystem("The-Corporation")

    val starshipCommand = akkaSystem.actorOf(StarshipCommand(), name = "Admiral-Reith")

    implicit val timeout = Timeout(Duration.create(20, SECONDS))
    val futureResponse = starshipCommand ? SeekOutNewLifeAndNewCivilisations(planetsToSurvey)

    val occupiedPlanets = Await.result(futureResponse, timeout.duration).asInstanceOf[Int]
    occupiedPlanets
  }
}
