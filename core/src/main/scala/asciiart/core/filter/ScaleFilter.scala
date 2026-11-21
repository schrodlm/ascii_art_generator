package asciiart.core.filter

import asciiart.core.image.AsciiArt

/**
 * Scales an AsciiArt image.
 *
 * @param scale Factor the output size is multiplied by: 2 doubles it, 0.5 halves it.
 */
class ScaleFilter(val scale: Double) extends AsciiArtFilter {

  /**
   * Applies scaling to the given AsciiArt image.
   *
   * @param image The AsciiArt image to scale.
   * @return Scaled AsciiArt image, at least one character wide and tall.
   * @throws IllegalArgumentException if scale is not a finite positive number, or the output
   *                                  would not fit in an array.
   */
  override def apply(image: AsciiArt): AsciiArt = {
    if (!(scale > 0) || scale.isInfinite) throw new IllegalArgumentException("Scale must be a finite positive number")

    val width = image.getWidth()
    val height = image.getHeight()
    val newWidth = math.max(1, (width * scale).toInt)
    val newHeight = math.max(1, (height * scale).toInt)
    if (newWidth.toLong * newHeight > Int.MaxValue) throw new IllegalArgumentException("Scale is too large")
    image.rearrange(newWidth, newHeight)((x, y) => (sourceCoord(x, width, newWidth), sourceCoord(y, height, newHeight)))
  }

  // The source coordinate under the centre of output coordinate `out`.
  private def sourceCoord(out: Int, size: Int, newSize: Int): Int =
    ((2L * out + 1) * size / (2L * newSize)).toInt
}
