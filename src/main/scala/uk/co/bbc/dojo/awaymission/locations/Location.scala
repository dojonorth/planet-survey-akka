package uk.co.bbc.dojo.awaymission.locations

trait Location

object StarshipBase extends Location {
  override def toString: String = "Starship Base"
}

case class Orbiting(planet: Planet) extends Location