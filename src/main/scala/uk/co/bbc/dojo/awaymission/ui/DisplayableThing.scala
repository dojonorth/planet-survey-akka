package uk.co.bbc.dojo.awaymission.ui

import uk.co.bbc.dojo.awaymission.locations.Location

trait DisplayableThing {
  def name: String

  def lastAction: String

  def location: Location
}
