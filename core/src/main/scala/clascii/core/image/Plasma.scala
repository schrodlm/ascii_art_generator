package clascii.core.image

import scala.util.Random

/**
 * Draws a plasma: three soft waves of grey laid over each other, different for
 * every seed. The seed fixes the waves on every platform, because
 * `scala.util.Random` is the same generator everywhere.
 */
object Plasma {

  private val width = 640
  private val height = 480

  /** A 640 by 480 plasma for the given seed. */
  def generate(seed: Long): RGBImage = {
    val random = new Random(seed)
    // Each wave turns 3.2 to 17.6 radians (about half to three cycles) across the
    // width. `down` is divided by the width as well, so both directions use the
    // same scale and the waves are not squashed on a non-square image.
    val waves = Array.fill(3) {
      val across = random.between(3.2, 17.6) / width
      val down = random.between(3.2, 17.6) / width
      val start = random.between(0.0, 2 * math.Pi)
      (across, down, start)
    }

    val data = Array.tabulate(width * height) { i =>
      val x = i % width
      val y = i / width
      val sum = waves.map { case (across, down, start) => math.sin(across * x + down * y + start) }.sum
      val grey = (128 + 127 * sum / 3).toInt
      0xFF000000 | (grey << 16) | (grey << 8) | grey
    }
    new RGBImage(width, height, data)
  }
}
