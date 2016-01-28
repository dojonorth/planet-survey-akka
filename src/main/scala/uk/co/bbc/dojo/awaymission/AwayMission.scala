package uk.co.bbc.dojo.awaymission

import akka.util.Timeout
import uk.co.bbc.dojo.awaymission.locations.Planet

import scala.concurrent.Await
import scala.concurrent.duration._

class AwayMission {
  def surveyPlanets(planetsToSurvey: List[Planet]): Int = {
    implicit val timeout = Timeout(Duration.create(20, SECONDS))
    val futureResponse = ???

    val occupiedPlanets = Await.result(futureResponse, timeout.duration).asInstanceOf[Int]
    occupiedPlanets
  }
}
