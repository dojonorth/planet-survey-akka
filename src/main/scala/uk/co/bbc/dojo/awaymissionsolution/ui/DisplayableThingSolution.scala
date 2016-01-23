package uk.co.bbc.dojo.awaymissionsolution.ui

import uk.co.bbc.dojo.awaymissionsolution.locations.LocationSolution

trait DisplayableThingSolution {
  def name: String

  def lastAction: String

  def location: LocationSolution
}
