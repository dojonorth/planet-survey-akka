package uk.co.bbc.dojo.awaymission.actors

import akka.actor.{ActorRef, Props}
import akka.event.LoggingReceive
import StarshipCommand.{SeekOutNewLifeAndNewCivilisations, SurveyResults}
import Starship.ExplorePlanet
import uk.co.bbc.dojo.awaymission.locations.{Planet, StarshipBase}

object StarshipCommand {
  case class SeekOutNewLifeAndNewCivilisations(planetsToExplore: List[Planet])
  case class SurveyResults(planet: Planet, alienLifePresent: Boolean)

  def apply(): Props = Props(new StarshipCommand)
}

class StarshipCommand() extends ActorWithLocation(StarshipBase) {
  private var externalResultActorRef: ActorRef = _

  private var planetsToExplore = Seq[Planet]()

  override def receive: Receive = LoggingReceive {
    case SeekOutNewLifeAndNewCivilisations(planetsToExplore: Seq[Planet]) => {
      externalResultActorRef = sender
      this.planetsToExplore = planetsToExplore

      planetsToExplore match {
        case Nil => sender ! -1
        case _ => {
          for(nextPlanetToExplore <- planetsToExplore) {
          }
        }
      }
    }
  }
}
