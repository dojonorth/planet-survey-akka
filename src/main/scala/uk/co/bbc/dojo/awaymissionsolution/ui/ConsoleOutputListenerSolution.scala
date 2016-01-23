package uk.co.bbc.dojo.awaymissionsolution.ui

import uk.co.bbc.dojo.awaymissionsolution.locations.LocationSolution

trait UIListenerSolution {
  def actionChangeNotification(displayable: DisplayableThingSolution)
  def locationChanageNotification(displayable: DisplayableThingSolution, oldLocation: LocationSolution)
}

object ConsoleOutputListenerSolution extends UIListenerSolution{
  override def locationChanageNotification(displayable: DisplayableThingSolution, oldLocation: LocationSolution) = {
    println(s"${displayable.name} moved to ${displayable.location} from $oldLocation")
  }

  override def actionChangeNotification(displayable: DisplayableThingSolution) = {
    println(s"${displayable.name} ${displayable.lastAction}")
  }
}
