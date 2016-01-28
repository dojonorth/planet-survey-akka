package uk.co.bbc.dojo.awaymission.actors

import akka.actor.{ActorRef, Props}
import akka.event.LoggingReceive
import uk.co.bbc.dojo.awaymission.actors.StarshipCommand.{SurveyResult, SeekOutNewLifeAndNewCivilisations}
import uk.co.bbc.dojo.awaymission.locations.{Planet, StarshipBase}

object StarshipCommand {
  case class SeekOutNewLifeAndNewCivilisations(planetsToExplore: List[Planet])
  case class SurveyResult(planet: Planet, alienLifePresent: Boolean)

  def apply(): Props = Props(new StarshipCommand)
}

class StarshipCommand() extends ActorWithLocation(StarshipBase) {
  // We'll maintain mutable state of planet(s depending on where you go with exercise 3) visited. Not very idiomatic Scala - but more accessible if you're not familiar with it.
  private var planetsToExplore = Seq[Planet]()

  private var externalResultActorRef: ActorRef = _

  override def receive: Receive = LoggingReceive {
    case SeekOutNewLifeAndNewCivilisations(planetsToExplore: Seq[Planet]) => {
      externalResultActorRef = sender
      this.planetsToExplore = planetsToExplore

      planetsToExplore match {
        case Nil => {
          // No planets, so return immediately.
          sender ! 0
          log.info(s"messaged $externalResultActorRef with a default value of 0 planets.")
        }
        case _ => {
          for(nextPlanetToExplore <- planetsToExplore) {
            // We'll nessage the ship for each planet.
          }
        }
      }
    }
    case SurveyResult(planet, alienLifePresent) => {
      // For now, we just always immediately respond with 0. Later we'll do some processing here.
      val numberOfInhabitedPlanets = 0

      externalResultActorRef ! numberOfInhabitedPlanets

      log.info(s"messaged $externalResultActorRef that there are $numberOfInhabitedPlanets inhabited planet(s).")
    }
  }
}
