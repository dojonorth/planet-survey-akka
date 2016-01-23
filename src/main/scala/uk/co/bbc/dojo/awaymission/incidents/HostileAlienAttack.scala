package uk.co.bbc.dojo.awaymission.incidents

case class HostileAlienAttack(message: String, planetOccupied: Boolean) extends Exception(message)
