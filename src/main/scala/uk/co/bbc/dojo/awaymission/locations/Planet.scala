package uk.co.bbc.dojo.awaymission.locations

import scala.util.{Failure, Success, Try}

trait Planet extends Location {
  def name: String
  def scanForLife: Try[Boolean]

  override def toString: String = name
}

case class ScannablePlanet(name: String, hasLife: Boolean) extends Planet {
  override def scanForLife: Try[Boolean] = Success(hasLife)
}

class ClangerBirdOfPreyAttacks extends Exception

case class ClangerPrime() extends Planet {
  override def name: String = "Clanger Prime"

  override def scanForLife: Try[Boolean] = Failure(new ClangerBirdOfPreyAttacks)
}

class UnscannableAtmosphereException extends Exception

case class UnscannablePlanet(name: String, hasLife: Boolean) extends Planet {
  override def scanForLife: Try[Boolean] = Failure(new UnscannableAtmosphereException)
}