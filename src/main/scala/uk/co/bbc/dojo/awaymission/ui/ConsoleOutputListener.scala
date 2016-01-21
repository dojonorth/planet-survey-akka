package uk.co.bbc.dojo.awaymission.ui

import uk.co.bbc.dojo.awaymission.locations.Location

trait UIListener {
  def actionChangeNotification(displayable: DisplayableThing)
  def locationChanageNotification(displayable: DisplayableThing, oldLocation: Location)
}

//TODO: Maybe change to not be an object and pass an instance around. Probably not just for the dojo though.
object ConsoleOutputListener extends UIListener{
  override def locationChanageNotification(displayable: DisplayableThing, oldLocation: Location) = {
    println(s"${displayable.name} moved to ${displayable.location} from $oldLocation")
  }

  override def actionChangeNotification(displayable: DisplayableThing) = {
    println(s"${displayable.name} ${displayable.lastAction}")
  }
}
