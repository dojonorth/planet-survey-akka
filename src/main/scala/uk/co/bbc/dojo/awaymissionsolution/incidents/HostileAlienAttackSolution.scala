package uk.co.bbc.dojo.awaymissionsolution.incidents

case class HostileAlienAttackSolution(message: String, planetOccupied: Boolean) extends Exception(message)
