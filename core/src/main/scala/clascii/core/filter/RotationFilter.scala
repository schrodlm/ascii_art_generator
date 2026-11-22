package clascii.core.filter

import clascii.core.image.RGBImage

/**
 * Rotates an RGB image clockwise by a multiple of 90 degrees.
 *
 * @param degrees The angle to turn clockwise; negative values turn anticlockwise.
 */
class RotationFilter(val degrees: Int) extends RGBImageFilter {

  /**
   * Applies the rotation to an RGBImage.
   *
   * @param image The RGBImage to rotate.
   * @return The rotated RGBImage.
   * @throws IllegalArgumentException if degrees is not a multiple of 90.
   */
  override def apply(image: RGBImage): RGBImage = {
    if (degrees % 90 != 0) throw new IllegalArgumentException("Rotation must be a multiple of 90 degrees")

    val width = image.getWidth()
    val height = image.getHeight()
    val data = image.getData()

    Math.floorMod(degrees / 90, 4) match {
      case 0 => image
      case 2 => new RGBImage(width, height, data.reverse)
      case quarterTurns =>
        // A quarter turn swaps width and height; output pixel (x, y) reads one source pixel.
        val newData = Array.tabulate(width * height) { i =>
          val x = i % height
          val y = i / height
          if (quarterTurns == 1) data((height - 1 - x) * width + y) // clockwise
          else data(x * width + (width - 1 - y)) // anticlockwise
        }
        new RGBImage(height, width, newData)
    }
  }
}
