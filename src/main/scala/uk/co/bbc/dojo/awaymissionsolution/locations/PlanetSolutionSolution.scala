package uk.co.bbc.dojo.awaymissionsolution.locations

import uk.co.bbc.dojo.awaymissionsolution.incidents.ClangerBirdOfPreyAttackSolution

import scala.util.{Failure, Success, Try}

trait PlanetSolutionSolution extends LocationSolution {
  def name: String
  def scanForLife: Try[Boolean]

  override def toString: String = name
}

case class ScannablePlanetSolution(name: String, hasLife: Boolean, timeToScanInMilliseconds: Int = 0) extends PlanetSolutionSolution {
  override def scanForLife: Try[Boolean] = {
    Thread.sleep(timeToScanInMilliseconds)
    Success(hasLife)
  }
}

object ClangerPrimeSolution extends PlanetSolutionSolution {
  override def name: String = "Clanger Prime"

  override def scanForLife: Try[Boolean] = Failure(new ClangerBirdOfPreyAttackSolution(true))
}

object AbandonedClangerOutpostSolution extends PlanetSolutionSolution {
  override def name: String = "A long abandoned Clanger outpost"

  override def scanForLife: Try[Boolean] = Failure(new ClangerBirdOfPreyAttackSolution(false))
}

