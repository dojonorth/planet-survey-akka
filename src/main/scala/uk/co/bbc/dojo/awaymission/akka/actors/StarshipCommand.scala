package uk.co.bbc.dojo.awaymission.akka.actors

import akka.actor.{ActorRef, Props, Actor}
import uk.co.bbc.dojo.awaymission.akka.actors.StarshipCommand.{StarshipSOS, SeekOutNewLifeAndNewCivilisations, SurveyResults}
import uk.co.bbc.dojo.awaymission.akka.actors.Starship.ExplorePlanet
import uk.co.bbc.dojo.awaymission.locations.{Planet, StarshipBase}

object StarshipCommand {
  case class SeekOutNewLifeAndNewCivilisations(planetsToExplore: List[Planet])
  case class SurveyResults(planet: Planet, alienLifePresent: Boolean)
  case class StarshipSOS(planet: Planet)

  def apply(): Props = Props(new StarshipCommand)
}

class StarshipCommand() extends DisplayableActor(StarshipBase) {
  private var externalResultActorRef: ActorRef = _ //TODO: Try and do this better.
  private var planetsToExplore = Seq[Planet]()
  private var uninhabitedPlanets = Seq[Planet]()
  private var inhabitedPlanets = Seq[Planet]()

  // The EnPrise is a child of Starship Command
  private val theEnPrise = context.actorOf(Starship(), "The-BBC-EnPrise")

  override def receive: Receive = {
    case SeekOutNewLifeAndNewCivilisations(planetsToExplore: Seq[Planet]) => {
      externalResultActorRef = sender
      this.planetsToExplore = planetsToExplore

      for(nextPlanetToExplore <- planetsToExplore) {
        val explorePlanetMessage = ExplorePlanet(nextPlanetToExplore)
        lastAction = s"messaged $theEnPrise with $explorePlanetMessage"
        theEnPrise ! ExplorePlanet(nextPlanetToExplore)
      }
    }
    case SurveyResults(planet, alienLifePresent) => {
      alienLifePresent match {
        case true => inhabitedPlanets = inhabitedPlanets :+ planet
        case false => uninhabitedPlanets = uninhabitedPlanets :+ planet
      }

      if (uninhabitedPlanets.length + inhabitedPlanets.length == planetsToExplore.length) {
        externalResultActorRef ! inhabitedPlanets.length
      }
    }
    case StarshipSOS(planet) => {
      inhabitedPlanets = inhabitedPlanets :+ planet // Well, if someone was there to blow them up then it must mean that the planet was occupied...
    }
  }
}
