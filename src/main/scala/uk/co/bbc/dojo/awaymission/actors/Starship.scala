package uk.co.bbc.dojo.awaymission.actors

import akka.actor.{ActorLogging, Props}
import StarshipCommand.{StarshipSOS, SurveyResults}
import Starship.ExplorePlanet
import akka.event.LoggingReceive
import uk.co.bbc.dojo.awaymission.locations.{Orbiting, StarshipBase, Planet}
import uk.co.bbc.dojo.awaymission.incidents.{SensorOverloadExplosion, HostileAlienAttack}

object Starship {
  case class ExplorePlanet(planetToExplore: Planet)

  def apply(): Props = Props(new Starship(Unarmed))
  def apply(armament: Armament): Props = Props(new Starship(armament))
}

class Starship(armament: Armament) extends ActorWithLocation(StarshipBase) with ActorLogging { // New ships are always created at starship base.
  override def receive: Receive = LoggingReceive {
    case ExplorePlanet(planet) => {
      location = Orbiting(planet)
      val alienLifePresent = checkForAlienLife(planet)
      sender ! SurveyResults(planet, alienLifePresent)
    }
  }

  private def checkForAlienLife(planet: Planet): Boolean = {
    log.info(s"$this scanning ${planet} for life")

    if (location != Orbiting(planet)) throw new SensorOverloadExplosion(location, planet)

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
        log.info(s"$this was destroyed by a ${reason.getMessage}")
        context.sender() ! StarshipSOS(deadlyPlanetMessage.planetToExplore)
      }
      case _ => log.info(s"$this was destroyed by a mysterious anomoly (unhandled exception)")
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
