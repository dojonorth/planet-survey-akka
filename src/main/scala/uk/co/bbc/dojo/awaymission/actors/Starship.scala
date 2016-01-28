package uk.co.bbc.dojo.awaymission.actors

import akka.actor.{ActorLogging, Props}
import akka.event.LoggingReceive
import uk.co.bbc.dojo.awaymission.actors.Starship.ExplorePlanet
import uk.co.bbc.dojo.awaymission.locations.{Orbiting, StarshipBase, Planet}
import uk.co.bbc.dojo.awaymission.incidents.HostileAlienAttack

object Starship {
  case class ExplorePlanet(planetToExplore: Planet)

  def apply(): Props = Props(new Starship(Unarmed))
  def apply(armament: Armament): Props = Props(new Starship(armament))
}

class Starship(armament: Armament) extends ActorWithLocation(StarshipBase) { // New ships are always created at starship base.
  override def receive: Receive = LoggingReceive {
    case ExplorePlanet(planet) => {
      //Do something in response to this.
    }
  }

  private def checkForAlienLife(planet: Planet): Boolean = {
    location = Orbiting(planet)

    log.info(s"is about to scan ${planet} for life")

    try {
      planet.scanForLife.get
    } catch {
      case e: HostileAlienAttack => {
        armament.handleAttack(e)
      }
    }
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
