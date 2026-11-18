package asciiart.core.filter

import asciiart.core.image.GrayscaleImage

/**
 * Changes the contrast of a Grayscale image by moving every value away from,
 * or towards, mid-grey.
 *
 * @param factor How far values move from mid-grey: 2 doubles the contrast, 0.5 halves it,
 *               1 leaves the image unchanged and 0 makes it all mid-grey.
 */
class ContrastFilter(val factor: Double) extends GrayscaleImageFilter {

  /**
   * Applies the contrast change to a GrayscaleImage.
   *
   * @param image The GrayscaleImage to adjust.
   * @return The adjusted GrayscaleImage.
   * @throws IllegalArgumentException if factor is negative or not a finite number.
   */
  override def apply(image: GrayscaleImage): GrayscaleImage = {
    if (!(factor >= 0) || factor.isInfinite) throw new IllegalArgumentException("Contrast must be a finite number of at least 0")

    val newData = image.getData().map { pixel =>
      val value = (image.getGrayscale(pixel) - 128) * factor + 128
      math.round(value max 0 min 255).toInt
    }

    new GrayscaleImage(image.getWidth(), image.getHeight(), newData)
  }
}
