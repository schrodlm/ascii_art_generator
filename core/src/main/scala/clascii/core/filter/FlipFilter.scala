package clascii.core.filter

import clascii.core.image.RGBImage


/**
 * Mirrors an RGB image. It runs before the image becomes characters, so the characters are drawn
 * from the mirrored picture: even the dots inside braille characters face the right way.
 *
 * @param flip The direction to flip the image in.
 */
class FlipFilter(val flip: Flip) extends RGBImageFilter {

  /**
   * Applies the flip filter to an RGBImage.
   *
   * @param image The RGBImage to flip.
   * @return The flipped RGBImage.
   */
  override def apply(image: RGBImage): RGBImage = {
    val width = image.getWidth()
    val height = image.getHeight()
    val data = image.getData()

    // The source pixel that output pixel (x, y) reads.
    val source: (Int, Int) => Int = flip match {
      case Flip.Vertical => (x, y) => (height - 1 - y) * width + x
      case Flip.Horizontal => (x, y) => y * width + (width - 1 - x)
    }
    new RGBImage(width, height, Array.tabulate(width * height)(i => data(source(i % width, i / width))))
  }
}
