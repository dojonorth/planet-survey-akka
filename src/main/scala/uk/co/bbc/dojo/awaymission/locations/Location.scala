package uk.co.bbc.dojo.awaymission.locations

trait Location

object StarshipBase extends Location {
  override def toString: String = "Starship Base"
}

case class Starship(name: String) extends Location

case class Bridge(starship: Starship) extends Location

case class TransporterRoom(starship: Starship) extends Location

case class Orbiting(planet: Planet) extends Location