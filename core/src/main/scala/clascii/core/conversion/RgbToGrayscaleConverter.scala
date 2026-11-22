package clascii.core.conversion
import clascii.core.image.{GrayscaleImage, RGBImage}

/**
 * Converts an RGBImage to a GrayscaleImage.
 */
private[core] class RgbToGrayscaleConverter() {

  /**
   * Converts the RGBImage to a GrayscaleImage.
   *
   * Uses a standard formula to calculate grayscale values from RGB.
   *
   * @param image The RGBImage to convert.
   * @return The converted GrayscaleImage.
   */
  def convert(image : RGBImage) : GrayscaleImage = {

    val width = image.getWidth()
    val height = image.getHeight()

    val grayscaleData = image.getData().map { pixel =>
      val (_, red, green, blue) = image.getPixelColor(pixel)

      // Calculate grayscale value and ensure it's within the 0-255 range
      val grayscalePixel = (0.3 * red + 0.59 * green + 0.11 * blue).toInt
      val clampedGrayscalePixel = grayscalePixel max 0 min 255

      clampedGrayscalePixel
    }

    new GrayscaleImage(width,height, grayscaleData)
  }
}
