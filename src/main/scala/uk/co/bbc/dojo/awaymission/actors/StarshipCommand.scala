package uk.co.bbc.dojo.awaymission.actors

import akka.actor.{ActorRef, Props}
import akka.event.LoggingReceive
import akka.routing.BalancingPool
import StarshipCommand.{StarshipSOS, SeekOutNewLifeAndNewCivilisations, SurveyResults}
import Starship.ExplorePlanet
import uk.co.bbc.dojo.awaymission.locations.{Planet, StarshipBase}

object StarshipCommand {
  case class SeekOutNewLifeAndNewCivilisations(planetsToExplore: List[Planet])
  case class SurveyResults(planet: Planet, alienLifePresent: Boolean)
  case class StarshipSOS(planet: Planet)

  def apply(): Props = Props(new StarshipCommand)
}

class StarshipCommand() extends ActorWithLocation(StarshipBase) {
  private var externalResultActorRef: ActorRef = _

  private var planetsToExplore = Seq[Planet]()
  private var uninhabitedPlanets = Seq[Planet]()
  private var inhabitedPlanets = Seq[Planet]()

  // private val theEnPrise = context.actorOf(Starship(), "The-BBC-EnPrise") //Used for parts 1 to 5

  private val fleetSize = 3 // Used for subsequent parts.
  private val starshipDispatcher = context.actorOf(Starship().withRouter(BalancingPool(fleetSize)), name = "En-Prise")

  private val theMerciless = context.actorOf(Starship(LaserGuns), "The-BBC-Merciless")

  override def receive: Receive = LoggingReceive {
    case SeekOutNewLifeAndNewCivilisations(planetsToExplore: Seq[Planet]) => {
      externalResultActorRef = sender
      this.planetsToExplore = planetsToExplore

      planetsToExplore match {
        case Nil => sender ! 0
        case _ => {
          for(nextPlanetToExplore <- planetsToExplore) {
            starshipDispatcher ! ExplorePlanet(nextPlanetToExplore)
          }
        }
      }
    }
    case SurveyResults(planet, alienLifePresent) => {
      alienLifePresent match {
        case true => inhabitedPlanets = inhabitedPlanets :+ planet
        case false => uninhabitedPlanets = uninhabitedPlanets :+ planet
      }

      if (uninhabitedPlanets.length + inhabitedPlanets.length == planetsToExplore.length) {
        externalResultActorRef ! inhabitedPlanets.length
      }
    }
    case StarshipSOS(planet) => {
      val explorePlanetMessage = ExplorePlanet(planet)
      theMerciless ! ExplorePlanet(planet)
      log.info(s"messaged $theMerciless with $explorePlanetMessage")
    }
  }
}
