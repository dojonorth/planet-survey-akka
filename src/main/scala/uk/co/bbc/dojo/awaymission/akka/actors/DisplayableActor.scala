package uk.co.bbc.dojo.awaymission.akka.actors

import akka.actor.Actor
import uk.co.bbc.dojo.awaymission.ui.{ConsoleOutputListener, UIListener, DisplayableThing}
import uk.co.bbc.dojo.awaymission.locations.Location

abstract class DisplayableActor(private var _location: Location, private var _lastAction: String = "Awaiting Messages")(implicit uiListener: UIListener = ConsoleOutputListener) extends Actor with DisplayableThing {
  override def name = self.toString()

  override def lastAction = _lastAction
  protected def lastAction_= (lastAction: String) {
    _lastAction = lastAction
    uiListener.actionChangeNotification(this)
  }

  override def location = _location
  protected def location_= (newLocation: Location) {
    val oldLocation = location
    _location = newLocation
    uiListener.locationChanageNotification(this, oldLocation)
  }
}
