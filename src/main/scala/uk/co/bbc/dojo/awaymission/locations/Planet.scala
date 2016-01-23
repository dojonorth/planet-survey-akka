package uk.co.bbc.dojo.awaymission.locations

import uk.co.bbc.dojo.awaymission.incidents.ClangerBirdOfPreyAttack

import scala.util.{Failure, Success, Try}

trait Planet extends Location {
  def name: String
  def scanForLife: Try[Boolean]

  override def toString: String = name
}

case class ScannablePlanet(name: String, hasLife: Boolean, timeToScanInMilliseconds: Int = 0) extends Planet {
  override def scanForLife: Try[Boolean] = {
    Thread.sleep(timeToScanInMilliseconds)
    Success(hasLife)
  }
}

case class ClangerPrime() extends Planet {
  override def name: String = "Clanger Prime"

  override def scanForLife: Try[Boolean] = Failure(new ClangerBirdOfPreyAttack)
}