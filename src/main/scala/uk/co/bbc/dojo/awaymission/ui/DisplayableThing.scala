package uk.co.bbc.dojo.awaymission.ui

import uk.co.bbc.dojo.awaymission.locations.Location

trait DisplayableThing {
  def name: String

  /* Return a description of what the actor is doing at the moment */
  def lastAction: String

  def location: Location
}
