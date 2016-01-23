package uk.co.bbc.dojo.awaymissionsolution.akka.actors

import akka.actor.Props
import uk.co.bbc.dojo.awaymissionsolution.akka.actors.StarshipCommandSolution.{StarshipSOSSolution, SurveyResultsSolution}
import uk.co.bbc.dojo.awaymissionsolution.akka.actors.StarshipSolution.ExplorePlanetSolution
import uk.co.bbc.dojo.awaymissionsolution.locations.{OrbitingSolution, StarshipBaseSolution, PlanetSolutionSolution}
import uk.co.bbc.dojo.awaymissionsolution.incidents.HostileAlienAttackSolution

object StarshipSolution {
  case class ExplorePlanetSolution(planetToExplore: PlanetSolutionSolution)

  def apply(): Props = Props(new StarshipSolution(UnarmedSolution))
  def apply(armament: ArmamentSolution): Props = Props(new StarshipSolution(armament))
}

class StarshipSolution(armament: ArmamentSolution) extends DisplayableActorSolution(StarshipBaseSolution) { // New ships are always created at starship base.
  override def receive: Receive = {
    case ExplorePlanetSolution(planet) => {
      travelTo(planet)
      val alienLifePresent = checkForAlienLife(planet)
      val surveyResultMessage = SurveyResultsSolution(planet, alienLifePresent)
      lastAction = s"messaged $sender with $surveyResultMessage"
      sender ! surveyResultMessage
    }
  }

  private def travelTo(planet: PlanetSolutionSolution) = {
    location = OrbitingSolution(planet)
  }

  private def checkForAlienLife(planet: PlanetSolutionSolution): Boolean = {
    lastAction = s"scanning ${planet} for life"

    try {
      planet.scanForLife.get
    } catch {
      case e: HostileAlienAttackSolution => {
        armament.handleAttack(e)
      }
    }
  }

  override def preRestart(reason: Throwable, message: Option[Any]) = {
    message match {
      case Some(deadlyPlanetMessage: ExplorePlanetSolution) => {
        lastAction = s"was destroyed by a ${reason.getMessage}"
        context.sender() ! StarshipSOSSolution(deadlyPlanetMessage.planetToExplore)
      }
      case _ => "was destroyed by a mysterious anomoly (unhandled exception)"
    }

    super.preRestart(reason, message) // Keep the parent behaviour
  }
}

trait ArmamentSolution {
  def handleAttack(attack: HostileAlienAttackSolution): Boolean
}
object UnarmedSolution extends ArmamentSolution {
  override def handleAttack(attack: HostileAlienAttackSolution) = {throw attack}
}
object LaserGunsSolution extends ArmamentSolution {
  override def handleAttack(attack: HostileAlienAttackSolution) = {attack.planetOccupied}
}
