package uk.co.bbc.dojo.actors.pi

class SingleThreadedPiCalculator(numberOfIterations: Long) extends PiCalculator {
  def calculatePi: Double = {
    LiebnizPi.evaluateLiebniz(0, numberOfIterations) * 4.0
  }
}
