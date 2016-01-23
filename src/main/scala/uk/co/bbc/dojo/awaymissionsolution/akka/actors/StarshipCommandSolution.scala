package uk.co.bbc.dojo.awaymissionsolution.akka.actors

import akka.actor.{ActorRef, Props, Actor}
import akka.routing.{BalancingPool, RoundRobinPool}
import uk.co.bbc.dojo.awaymissionsolution.akka.actors.StarshipCommandSolution.{StarshipSOSSolution, SeekOutNewLifeAndNewCivilisationsSolution, SurveyResultsSolution}
import uk.co.bbc.dojo.awaymissionsolution.akka.actors.StarshipSolution.ExplorePlanetSolution
import uk.co.bbc.dojo.awaymissionsolution.locations.{PlanetSolutionSolution, StarshipBaseSolution}

object StarshipCommandSolution {
  case class SeekOutNewLifeAndNewCivilisationsSolution(planetsToExplore: List[PlanetSolutionSolution])
  case class SurveyResultsSolution(planet: PlanetSolutionSolution, alienLifePresent: Boolean)
  case class StarshipSOSSolution(planet: PlanetSolutionSolution)

  def apply(): Props = Props(new StarshipCommandSolution)
}

class StarshipCommandSolution() extends DisplayableActorSolution(StarshipBaseSolution) {
  private var externalResultActorRef: ActorRef = _ //TODO: Try and do this better.
  private var planetsToExplore = Seq[PlanetSolutionSolution]()
  private var uninhabitedPlanets = Seq[PlanetSolutionSolution]()
  private var inhabitedPlanets = Seq[PlanetSolutionSolution]()

  private val theEnPrise = context.actorOf(StarshipSolution(), "The-BBC-EnPrise") //Used for parts 1 to 5

  private val fleetSize = 3 // Used for subsequent parts.
  private val starshipDispatcher = context.actorOf(StarshipSolution().withRouter(BalancingPool(fleetSize)), name = "En-Prise")

  private val theMerciless = context.actorOf(StarshipSolution(LaserGunsSolution), "The-BBC-Merciless")

  override def receive: Receive = {
    case SeekOutNewLifeAndNewCivilisationsSolution(planetsToExplore: Seq[PlanetSolutionSolution]) => {
      externalResultActorRef = sender
      this.planetsToExplore = planetsToExplore

      for(nextPlanetToExplore <- planetsToExplore) {
        val explorePlanetMessage = ExplorePlanetSolution(nextPlanetToExplore)
        lastAction = s"messaged $starshipDispatcher with $explorePlanetMessage"
        starshipDispatcher ! ExplorePlanetSolution(nextPlanetToExplore)
      }
    }
    case SurveyResultsSolution(planet, alienLifePresent) => {
      alienLifePresent match {
        case true => inhabitedPlanets = inhabitedPlanets :+ planet
        case false => uninhabitedPlanets = uninhabitedPlanets :+ planet
      }

      if (uninhabitedPlanets.length + inhabitedPlanets.length == planetsToExplore.length) {
        externalResultActorRef ! inhabitedPlanets.length
      }
    }
    case StarshipSOSSolution(planet) => {
      val explorePlanetMessage = ExplorePlanetSolution(planet)
      lastAction = s"messaged $theMerciless with $explorePlanetMessage"
      theMerciless ! explorePlanetMessage
    }
  }
}
