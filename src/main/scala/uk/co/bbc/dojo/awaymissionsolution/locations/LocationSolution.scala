package uk.co.bbc.dojo.awaymissionsolution.locations

trait LocationSolution

object StarshipBaseSolution extends LocationSolution {
  override def toString: String = "Starship Base"
}

case class OrbitingSolution(planet: PlanetSolutionSolution) extends LocationSolution