package uk.co.bbc.dojo.actors.pi

trait PiCalculator {
  def calculatePi(): Double
}

object LiebnizPi {
  def evaluateLiebniz(startTerm: Long, noIterations: Long): Double = {
    var sum = 0.0
    var term = startTerm

    while(term < startTerm + noIterations) {
      val signDivident = 1.0 - ((term % 2) * 2.0)
      val divisor = 1 + (term * 2.0)
      sum += (signDivident / divisor)
      term += 1
    }

    sum
  }
}
