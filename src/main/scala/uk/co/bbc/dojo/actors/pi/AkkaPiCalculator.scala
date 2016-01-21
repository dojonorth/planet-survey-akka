package uk.co.bbc.dojo.actors.pi

import akka.actor.ActorSystem
import akka.actor._
import akka.routing.RoundRobinPool
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration.{Duration, MINUTES}
import scala.concurrent.Await

/**
 * Based on code from: http://doc.akka.io/docs/akka/2.0/intro/getting-started-first-scala.html
 */
class AkkaPiCalculator(numberOfIterations: Long) extends PiCalculator {
  private val numberOfWorkerActors = 8
  private val iterationsPerActorMessage = 1000000l

  override def calculatePi: Double = {
    //Create the Akka system.
    val system = ActorSystem("Pi-System")

    // The system creates a top-level actor automatically that all user created actors extend from. We now create our master actor that will be a child of it.
    val master = system.actorOf(Props(new Master(numberOfWorkerActors, iterationsPerActorMessage, numberOfIterations)), name = "master")

    // Start the calculation. We could use the exclamation mark, 'fire and forget' (don't block) message notation, but instead we want a response, so we use the ?
    implicit val timeout = Timeout(Duration.create(1, MINUTES))
    val futureResponse = master ? Calculate

    // The Actor response isn't typed (it's a unit), so we have to cast it, annoyingly.
    val akkaResult: PiApproximation = Await.result(futureResponse, timeout.duration).asInstanceOf[PiApproximation]
    akkaResult.pi
  }
}

  class Master(numberOfWorkerActors: Int, iterationsPerActorMessage: Long, totalNumberOfIterations: Long) extends Actor {
    private var actorToSendResultTo: ActorRef = _ //TODO: Try and do this a slicker way.
    private var currentPi: Double = 0
    private var numberOfResultsRecieved: Int = 0

    private val workerRouter = context.actorOf(Props[Worker].withRouter(RoundRobinPool(numberOfWorkerActors)), name = "workerRouter")

    def receive = {
      case Calculate =>
        actorToSendResultTo = sender
        for (i <- 0l until totalNumberOfMessages) workerRouter ! Work(i * iterationsPerActorMessage, iterationsPerActorMessage)
      case Result(value) =>
        currentPi += value
        numberOfResultsRecieved += 1
        if (numberOfResultsRecieved == totalNumberOfMessages) {
          // Send the result to the listener
          actorToSendResultTo ! PiApproximation(currentPi * 4.0)
          // Stops this actor and all its supervised children
          context.stop(self)
        }
    }

    private def totalNumberOfMessages: Long = {
      (totalNumberOfIterations % iterationsPerActorMessage) match {
        case 0 => totalNumberOfIterations / iterationsPerActorMessage
        case _ => totalNumberOfIterations / iterationsPerActorMessage + 1
      }
    }
  }

  sealed trait PiMessage
  case object Calculate extends PiMessage
  case class Work(start: Long, nrOfElements: Long) extends PiMessage
  case class Result(value: Double) extends PiMessage
  case class PiApproximation(pi: Double)

  class Worker extends Actor{
    override def receive = {
      case Work(start, nrOfElements) =>
        sender ! Result(LiebnizPi.evaluateLiebniz(start, nrOfElements)) // perform the work
    }
  }
