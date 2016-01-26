package uk.co.bbc.dojo.awaymission.actors

import akka.actor.{ActorRef, Props, Actor}
import akka.routing.{BalancingPool, RoundRobinPool}
import StarshipCommand.{StarshipSOS, SeekOutNewLifeAndNewCivilisations, SurveyResults}
import Starship.ExplorePlanet
import uk.co.bbc.dojo.awaymission.locations.{Planet, StarshipBase}

import scala.collection.immutable.Stream.Empty

object StarshipCommand {
  case class SeekOutNewLifeAndNewCivilisations(planetsToExplore: List[Planet])
  case class SurveyResults(planet: Planet, alienLifePresent: Boolean)
  case class StarshipSOS(planet: Planet)

  def apply(): Props = Props(new StarshipCommand)
}

//TODO: Want to automatically list passed messages.
class StarshipCommand() extends DisplayableActor(StarshipBase) {
  private var externalResultActorRef: ActorRef = _ //TODO: Try and do this better.
  private var planetsToExplore = Seq[Planet]()
  private var uninhabitedPlanets = Seq[Planet]()
  private var inhabitedPlanets = Seq[Planet]()

  // private val theEnPrise = context.actorOf(Starship(), "The-BBC-EnPrise") //Used for parts 1 to 5

  private val fleetSize = 3 // Used for subsequent parts.
  private val starshipDispatcher = context.actorOf(Starship().withRouter(BalancingPool(fleetSize)), name = "En-Prise")

  private val theMerciless = context.actorOf(Starship(LaserGuns), "The-BBC-Merciless")

  override def receive: Receive = {
    case SeekOutNewLifeAndNewCivilisations(planetsToExplore: Seq[Planet]) => {
      externalResultActorRef = sender
      this.planetsToExplore = planetsToExplore

      planetsToExplore match {
        case Nil => sender ! 0
        case _ => {
          for(nextPlanetToExplore <- planetsToExplore) {
            val explorePlanetMessage = ExplorePlanet(nextPlanetToExplore)
            lastAction = s"messaged $starshipDispatcher with $explorePlanetMessage"
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
      lastAction = s"messaged $theMerciless with $explorePlanetMessage"
      theMerciless ! ExplorePlanet(planet)
    }
  }
}
