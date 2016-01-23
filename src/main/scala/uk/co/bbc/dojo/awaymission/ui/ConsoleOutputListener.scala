package uk.co.bbc.dojo.awaymission.ui

import uk.co.bbc.dojo.awaymission.locations.Location

trait UIListener {
  def actionChangeNotification(displayable: DisplayableThing)
  def locationChanageNotification(displayable: DisplayableThing, oldLocation: Location)
}

object ConsoleOutputListener extends UIListener{
  override def locationChanageNotification(displayable: DisplayableThing, oldLocation: Location) = {
    println(s"${displayable.name} moved to ${displayable.location} from $oldLocation")
  }

  override def actionChangeNotification(displayable: DisplayableThing) = {
    println(s"${displayable.name} ${displayable.lastAction}")
  }
}
