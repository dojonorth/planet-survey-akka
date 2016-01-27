package uk.co.bbc.dojo.awaymission.incidents

import uk.co.bbc.dojo.awaymission.locations.Location

class SensorOverloadExplosion(actual: Location, intended: Location) extends Exception {
  override def getMessage: String = {
    s"Attempting to scan $intended from $actual overloaded the main reactor with disasterous consequences!"
  }
}
