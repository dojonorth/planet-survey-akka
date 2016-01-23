package uk.co.bbc.dojo.awaymissionsolution.akka.actors

import akka.actor.Actor
import uk.co.bbc.dojo.awaymissionsolution.ui.{ConsoleOutputListenerSolution, UIListenerSolution, DisplayableThingSolution}
import uk.co.bbc.dojo.awaymissionsolution.locations.LocationSolution

abstract class DisplayableActorSolution(private var _location: LocationSolution, private var _lastAction: String = "Awaiting Messages")(implicit uiListener: UIListenerSolution = ConsoleOutputListenerSolution) extends Actor with DisplayableThingSolution {
  override def name = self.toString()

  override def lastAction = _lastAction
  protected def lastAction_= (lastAction: String) {
    _lastAction = lastAction
    uiListener.actionChangeNotification(this)
  }

  override def location = _location
  protected def location_= (newLocation: LocationSolution) {
    val oldLocation = location
    _location = newLocation
    uiListener.locationChanageNotification(this, oldLocation)
  }
}
