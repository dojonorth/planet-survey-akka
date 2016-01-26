package uk.co.bbc.dojo.awaymission.actors

import akka.actor.Props
import StarshipCommand.{StarshipSOS, SurveyResults}
import Starship.ExplorePlanet
import uk.co.bbc.dojo.awaymission.locations.{Orbiting, StarshipBase, Planet}
import uk.co.bbc.dojo.awaymission.incidents.HostileAlienAttack

object Starship {
  case class ExplorePlanet(planetToExplore: Planet)

  def apply(): Props = Props(new Starship(Unarmed))
  def apply(armament: Armament): Props = Props(new Starship(armament))
}

class Starship(armament: Armament) extends DisplayableActor(StarshipBase) { // New ships are always created at starship base.
  override def receive: Receive = {
    case ExplorePlanet(planet) => {
      travelTo(planet)
      val alienLifePresent = checkForAlienLife(planet)
      val surveyResultMessage = SurveyResults(planet, alienLifePresent)
      lastAction = s"messaged $sender with $surveyResultMessage"
      sender ! surveyResultMessage
    }
  }

  private def travelTo(planet: Planet) = {
    location = Orbiting(planet)
  }

  private def checkForAlienLife(planet: Planet): Boolean = {
    lastAction = s"scanning ${planet} for life"

    try {
      planet.scanForLife.get
    } catch {
      case e: HostileAlienAttack => {
        armament.handleAttack(e)
      }
    }
  }

  override def preRestart(reason: Throwable, message: Option[Any]) = {
    message match {
      case Some(deadlyPlanetMessage: ExplorePlanet) => {
        lastAction = s"was destroyed by a ${reason.getMessage}"
        context.sender() ! StarshipSOS(deadlyPlanetMessage.planetToExplore)
      }
      case _ => "was destroyed by a mysterious anomoly (unhandled exception)"
    }

    super.preRestart(reason, message) // Keep the parent behaviour
  }
}

trait Armament {
  def handleAttack(attack: HostileAlienAttack): Boolean
}
object Unarmed extends Armament {
  override def handleAttack(attack: HostileAlienAttack) = {throw attack}
}
object LaserGuns extends Armament {
  override def handleAttack(attack: HostileAlienAttack) = {attack.planetOccupied}
}
