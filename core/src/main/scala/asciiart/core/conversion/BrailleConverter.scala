package asciiart.core.conversion

import asciiart.core.characters.{Braille, Dither}
import asciiart.core.image.{AsciiArt, GrayscaleImage}

/**
 * Draws braille characters, one for every 2 by 4 grey pixels, showing eight times the detail of
 * one character per grey. A dot is raised where the picture is dark, as ink would be.
 *
 * @param dither How to decide which dots to raise.
 */
class BrailleConverter(val dither: Dither = Dither.Ordered) extends CharacterConverter {

  override def cellWidth: Int = 2

  override def cellHeight: Int = 4

  /**
   * @inheritdoc
   * @throws IllegalArgumentException if the image is not a whole number of 2 by 4 cells.
   */
  def convert(image: GrayscaleImage): AsciiArt = {
    val width = image.getWidth()
    val height = image.getHeight()
    if (width % 2 != 0 || height % 4 != 0) {
      throw new IllegalArgumentException("A braille image must be a whole number of 2 by 4 cells")
    }
    val raised = raisedDots(image)
    val columns = width / 2
    val characters = Array.tabulate(columns * (height / 4)) { i =>
      val left = i % columns * 2
      val top = i / columns * 4
      val dots = for (x <- 0 until 2; y <- 0 until 4 if raised((top + y) * width + left + x)) yield Braille.Dot(x)(y)
      Braille.Blank + dots.sum
    }
    new AsciiArt(columns, height / 4, characters)
  }

  private def raisedDots(image: GrayscaleImage): Array[Boolean] = {
    val width = image.getWidth()
    val greys = image.getData().map(image.getGrayscale)
    dither match {
      case Dither.Threshold => greys.map(_ < 128)
      case Dither.Ordered =>
        Array.tabulate(greys.length)(i => greys(i) < BrailleConverter.Thresholds(i / width % 4)(i % width % 4))
      case Dither.Diffusion => BrailleConverter.diffused(greys, width, image.getHeight())
    }
  }
}

object BrailleConverter {

  // A 4 by 4 Bayer matrix scaled to 8, 24, ..., 248, the middles of 16 equal bands of grey, spread
  // evenly over every 4 by 4 square, so any grey raises a fair share of its dots.
  private val Thresholds =
    Array(Array(0, 8, 2, 10), Array(12, 4, 14, 6), Array(3, 11, 1, 9), Array(15, 7, 13, 5)).map(_.map(_ * 16 + 8))

  // Floyd–Steinberg: row by row, each pixel becomes a dot or not, and the difference between its
  // grey and black or white is shared out among the neighbours not yet visited.
  private def diffused(greys: Array[Int], width: Int, height: Int): Array[Boolean] = {
    val values = greys.map(_.toDouble)
    val raised = new Array[Boolean](values.length)
    for (i <- values.indices) {
      val x = i % width
      val y = i / width
      raised(i) = values(i) < 128
      val error = values(i) - (if (raised(i)) 0 else 255)
      def pass(dx: Int, dy: Int, share: Double): Unit =
        if (x + dx >= 0 && x + dx < width && y + dy < height) values((y + dy) * width + x + dx) += error * share
      pass(1, 0, 7.0 / 16)
      pass(-1, 1, 3.0 / 16)
      pass(0, 1, 5.0 / 16)
      pass(1, 1, 1.0 / 16)
    }
    raised
  }
}
