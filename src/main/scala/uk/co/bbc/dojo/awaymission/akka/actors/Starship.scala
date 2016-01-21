package uk.co.bbc.dojo.awaymission.akka.actors

import akka.actor.Props
import uk.co.bbc.dojo.awaymission.akka.actors.StarshipCommand.SurveyResults
import uk.co.bbc.dojo.awaymission.akka.actors.Starship.ExplorePlanet
import uk.co.bbc.dojo.awaymission.locations.{Orbiting, StarshipBase, Planet}

object Starship {
  case class ExplorePlanet(planetToExplore: Planet)

  def apply(): Props = Props(new Starship())
}

// New ships are always created at starship base.
class Starship() extends DisplayableActor(StarshipBase){
  override def receive: Receive = {
    case ExplorePlanet(planet) => {
      travelTo(planet)
      val alienLifePresent = checkForAlienLife(planet)
      val surveyResultMessage = SurveyResults(planet, alienLifePresent)
      lastAction = s"messaged [$sender] with [$surveyResultMessage]"
      sender ! surveyResultMessage
    }
  }

  private def travelTo(planet: Planet) = {
    location = Orbiting(planet)
  }

  //TODO: Will get more complicated. Change to hide alien life on planet interface and include scan method.
  private def checkForAlienLife(planet: Planet): Boolean = {
    lastAction = s"scanning ${planet} for life"
    planet.scanForLife.get
  }
}
