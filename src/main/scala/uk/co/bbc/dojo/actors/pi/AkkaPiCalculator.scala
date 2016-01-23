package uk.co.bbc.dojo.actors.pi

import akka.actor.ActorSystem
import akka.actor._
import akka.routing.RoundRobinPool
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration.{Duration, MINUTES}
import scala.concurrent.Await

/**
 * Based (a bit) on code from: http://doc.akka.io/docs/akka/2.0/intro/getting-started-first-scala.html
 */
class AkkaPiCalculator(numberOfIterations: Long) extends PiCalculator {
  private val numberOfWorkerActors = 8
  private val iterationsPerActorMessage = 1000000l

  override def calculatePi: Double = {
    // Create the Akka system. There should be only one. Akka component names should not feature spaces or non-alphanumeric characters.
    val system = ActorSystem("Pi-System")

    // Create our coordinating master to be a top-level actor - i.e. one that sits directly below the Akka System (technically, it sits below the top-level guardian. See here for more info http://doc.akka.io/docs/akka/2.4.1/general/supervision.html)
    val master = system.actorOf(Props(new Master(numberOfWorkerActors, iterationsPerActorMessage, numberOfIterations)), name = "master")

    // Message the master actor to start the calculation. '?' is override to send a message that we expect a response from (the Akka 'Ask' pattern).
    // To send a non-blocking 'fire and forget' message, which is the norm, use '!' e.g. '<actor> ! <message>
    implicit val timeout = Timeout(Duration.create(1, MINUTES))
    val futureResponse = master ? Calculate

    // The Actor response isn't typed (it's a unit), so we have to cast it, annoyingly.
    val akkaResult: PiApproximation = Await.result(futureResponse, timeout.duration).asInstanceOf[PiApproximation]
    akkaResult.pi
  }
}

/**
 * The master is the coordinating actor and is a freestanding, autonomous actor that communicates purely in terms of messages.
 */
class Master(numberOfWorkerActors: Int, iterationsPerActorMessage: Long, totalNumberOfIterations: Long) extends Actor {
  private var actorToSendResultTo: ActorRef = _
  private var currentPi: Double = 0
  private var numberOfResultsRecieved: Int = 0

  def receive = {
    case Calculate =>
      // Keep tract of the actor that we will be sending the result to (a temporary one created to fulfill the ask pattern) TODO: Add link for further detail.
      actorToSendResultTo = sender

      // We create a worker router. This will spawn worker actors and dispatch messages to them in turn using a round robin strategy.
      // Note that each actor has a mailbox used to buffer messages received as it processes the current message.
      val workerRouter = context.actorOf(Props[Worker].withRouter(RoundRobinPool(numberOfWorkerActors)), name = "workerRouter")
      for (i <- 0l until totalNumberOfMessages) workerRouter ! Work(i * iterationsPerActorMessage, iterationsPerActorMessage)
    case Result(value) =>
      //When we receive a result back from a worker, we update our accumulators.
      currentPi += value
      numberOfResultsRecieved += 1

      if (numberOfResultsRecieved == totalNumberOfMessages) {
        // When all of the results are in, then message whoever kicked off the calculation with the final result.
        actorToSendResultTo ! PiApproximation(currentPi * 4.0)

        // Stops this actor and all its supervised children. Will happen anyway when the Akka system stops, so not really needed, but good practice.
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

// The worker class. Each one will calculate Pi for a specific range of the 'niz
class Worker extends Actor{
  override def receive = {
    case Work(start, nrOfElements) =>
      //Each actor has a context which provides lots of useful info / functionality. Prior to the receive method being called the sender is set in it.
      context.sender ! Result(LiebnizPi.evaluateLiebniz(start, nrOfElements))
  }
}

// All of the messages we'll use. Normally, these would be defined with the actor classes that handle them.
// Note that not making message immutable is *really* bad practice and shouldn't be done.
sealed trait PiMessage
case object Calculate extends PiMessage
case class Work(start: Long, nrOfElements: Long) extends PiMessage
case class Result(value: Double) extends PiMessage
case class PiApproximation(pi: Double)
