package uk.co.bbc.dojo.awaymission.actors

import akka.actor.{ActorRef, Props}
import akka.event.LoggingReceive
import uk.co.bbc.dojo.awaymission.actors.StarshipCommand.{SurveyResults, SeekOutNewLifeAndNewCivilisations}
import uk.co.bbc.dojo.awaymission.locations.{Planet, StarshipBase}

object StarshipCommand {
  case class SeekOutNewLifeAndNewCivilisations(planetsToExplore: List[Planet])
  case class SurveyResults(planet: Planet, alienLifePresent: Boolean)

  def apply(): Props = Props(new StarshipCommand)
}

class StarshipCommand() extends ActorWithLocation(StarshipBase) {
  // We'll maintain mutable lists of planets visited. Not very idiomatic Scala - but more accessible if you're not familiar with it.
  private var planetsToExplore = Seq[Planet]()
  private var occupiedPlanets = Seq[Planet]()
  private var unoccupiedPlanets = Seq[Planet]()

  private var externalResultActorRef: ActorRef = _


  private val surrveyShip = ??? // Create the En Prise here.

  override def receive: Receive = LoggingReceive {
    case SeekOutNewLifeAndNewCivilisations(planetsToExplore: Seq[Planet]) => {
      externalResultActorRef = sender
      this.planetsToExplore = planetsToExplore

      planetsToExplore match {
        case Nil => sender ! 0 // No planets, so return immediately.
        case _ => {
          for(nextPlanetToExplore <- planetsToExplore) {
            // Message the ship for each planet.
          }
        }
      }
    }
    case SurveyResults(planet, alienLifePresent) => {
      // For now, we just always immediately respond with 0. Later we'll do some processing here.

      externalResultActorRef ! 0
    }
  }
}
