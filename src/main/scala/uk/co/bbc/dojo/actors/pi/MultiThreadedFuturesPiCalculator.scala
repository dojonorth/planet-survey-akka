package uk.co.bbc.dojo.actors.pi

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class MultiThreadedFuturesPiCalculator(numberOfTerms: Long) extends PiCalculator {
  private val iterationsPerFuture = 1000000l
  override def calculatePi: Double = {
    import ExecutionContext.Implicits.global //Will have the same number of threads as you have cores.

    val blockIterationStarts = 0l to numberOfTerms by iterationsPerFuture // Will over calculate slightly (round up)
    val evalRanges = blockIterationStarts.map(blockIterationStart => Future{LiebnizPi.evaluateLiebniz(blockIterationStart, iterationsPerFuture)})
    Await.result(Future.sequence(evalRanges), Duration.create(1, MINUTES)).foldLeft(0.0)(_ + _) * 4.0
  }
}
