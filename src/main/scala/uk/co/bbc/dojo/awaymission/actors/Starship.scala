package uk.co.bbc.dojo.awaymission.actors

import akka.actor.{ActorLogging, Props}
import akka.event.LoggingReceive
import uk.co.bbc.dojo.awaymission.locations.{Orbiting, StarshipBase, Planet}
import uk.co.bbc.dojo.awaymission.incidents.{SensorOverloadExplosion, HostileAlienAttack}

object Starship {
  def apply(): Props = Props(new Starship(Unarmed))
  def apply(armament: Armament): Props = Props(new Starship(armament))
}

class Starship(armament: Armament) extends ActorWithLocation(StarshipBase) with ActorLogging { // New ships are always created at starship base.
  override def receive: Receive = LoggingReceive {
    case anyOldMessage => log.info(s"Don't know what to do with $anyOldMessage")
  }

  private def checkForAlienLife(planet: Planet): Boolean = {
    log.info(s"$this scanning ${planet} for life")

    location = Orbiting(planet)

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
