package uk.co.bbc.dojo.awaymission.actors

import akka.actor.{ActorLogging, Actor}
import uk.co.bbc.dojo.awaymission.locations.Location

abstract class ActorWithLocation(private var _location: Location) extends Actor with ActorLogging {
  protected def location = _location

  protected def location_= (newLocation: Location) {
    val oldLocation = location
    _location = newLocation
    log.info(s"moved from $oldLocation to $newLocation")
  }
}
