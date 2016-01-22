package uk.co.bbc.dojo.awaymission.akka.actors

import akka.actor.Props
import uk.co.bbc.dojo.awaymission.akka.actors.StarshipCommand.{StarshipSOS, SurveyResults}
import uk.co.bbc.dojo.awaymission.akka.actors.Starship.ExplorePlanet
import uk.co.bbc.dojo.awaymission.locations.{Orbiting, StarshipBase, Planet}
import uk.co.bbc.dojo.awaymission.incidents.HostileAlienAttack

object Starship {
  case class ExplorePlanet(planetToExplore: Planet)

  def apply(): Props = Props(new Starship(Unarmed))
  def apply(armament: Armament): Props = Props(new Starship(armament))
}

trait Armament {
  def handleAttack(attack: HostileAlienAttack): String
}
object Unarmed extends Armament {
  override def handleAttack(attack: HostileAlienAttack) = {throw attack}
}
object LaserGuns extends Armament {
  override def handleAttack(attack: HostileAlienAttack) = {
    "Their puny weapons were no match for our laser guns."
  }
}

// New ships are always created at starship base.
class Starship(armament: Armament) extends DisplayableActor(StarshipBase){
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

  //TODO: Will get more complicated. Change to hide alien life on planet interface and include scan method.
  private def checkForAlienLife(planet: Planet): Boolean = {
    lastAction = s"scanning ${planet} for life"

    try {
      planet.scanForLife.get
    } catch {
      case e: HostileAlienAttack => {
        armament.handleAttack(e)
        true // There was life - they shot at us...
      }
    }
  }

  // TODO: Maybe change this to be done by throwing a general exception that we wrap the killer one with in receive that holds the
  //       details of the planet we were scanning.
  override def preRestart(reason: Throwable, message: Option[Any]) = {
    super.preRestart(reason, message) // Keep the parent behaviour

    message match {
      case Some(deadlyPlanetMessage: ExplorePlanet) => {
        val finalSOS = StarshipSOS(deadlyPlanetMessage.planetToExplore)
        lastAction = s"messaged $context.parent with $finalSOS"
        context.parent ! finalSOS
      }
      case _ => //Otherwise do nothing.
    }
  }
}