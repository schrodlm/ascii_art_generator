package asciiart.core.filter

import asciiart.core.image.GrayscaleImage

/**
 * Adjusts the brightness of a Grayscale image.
 *
 * @param value The amount to adjust brightness by.
 */
class BrightnessFilter(val value: Int) extends GrayscaleImageFilter {

  /**
   * Applies a brightness adjustment to a GrayscaleImage.
   *
   * @param image The GrayscaleImage to adjust.
   * @return The brightness-adjusted GrayscaleImage.
   */
  override def apply(image: GrayscaleImage): GrayscaleImage = {
    val new_data = image.getData().map { pixel =>

      // Clamped first, so a huge value cannot overflow; beyond ±255 every grey is black or white anyway.
      val grayscale_value = image.getGrayscale(pixel) + (value max -255 min 255)
      val clampedGrayscaleValue = grayscale_value max 0 min 255

      clampedGrayscaleValue
    }

    new GrayscaleImage(image.getWidth(), image.getHeight(), new_data)
  }
}
