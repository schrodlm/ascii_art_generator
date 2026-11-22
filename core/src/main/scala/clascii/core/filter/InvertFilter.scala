package clascii.core.filter

import clascii.core.image.GrayscaleImage

/**
 * Inverts a Grayscale image, so black becomes white and white becomes black.
 */
class InvertFilter extends GrayscaleImageFilter {

  /**
   * Applies the inversion to a GrayscaleImage.
   *
   * @param image The GrayscaleImage to invert.
   * @return The inverted GrayscaleImage.
   */
  override def apply(image: GrayscaleImage): GrayscaleImage = {
    val newData = image.getData().map(pixel => 255 - image.getGrayscale(pixel))

    new GrayscaleImage(image.getWidth(), image.getHeight(), newData)
  }
}
