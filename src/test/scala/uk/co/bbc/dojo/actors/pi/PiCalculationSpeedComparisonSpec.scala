package uk.co.bbc.dojo.actors.pi

import org.scalatest.{FunSpec, Matchers}
import scala.math.{pow, Pi}

class PiCalculationSpeedComparisonSpec extends FunSpec with Matchers {
val NO_DECIMAL_PLACES_TO_CALCULATE_TO = 9
val ACCURACY_DELTA = pow(10, -NO_DECIMAL_PLACES_TO_CALCULATE_TO)

  describe("The Time It Takes To Calculate Pi") {
    val numberOfIterations = 2000000000l

    it("should be quite slow when using a single thread") {
      val singleThreadedPi = new SingleThreadedPiCalculator(numberOfIterations)
      val pi = runTimedTest(singleThreadedPi)
      assertIsPi(pi)
    }

    it("should be fast using futures") {
      val multiThreadedPi = new MultiThreadedFuturesPiCalculator(numberOfIterations)
      val pi = runTimedTest(multiThreadedPi)
      assertIsPi(pi)
    }

    it("should be fast using actors") {
      val actorBasedPi = new AkkaPiCalculator(numberOfIterations)
      val pi = runTimedTest(actorBasedPi)
      assertIsPi(pi)
    }
  }

  private def runTimedTest(piCalculator: PiCalculator) = {
    val startTime = System.currentTimeMillis()

    val pi = piCalculator.calculatePi

    val endTime = System.currentTimeMillis()
    val totalTime = (endTime - startTime) / 1000.0

    println("%s took %.1f seconds to run".format(piCalculator.getClass.getSimpleName, totalTime))

    pi
  }

  private def assertIsPi(maybePi: Double) = {
    maybePi should be(Pi +- ACCURACY_DELTA) //+- = new form of plusOrMinus
  }
}
