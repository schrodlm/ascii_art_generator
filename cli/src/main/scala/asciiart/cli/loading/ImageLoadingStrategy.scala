package asciiart.cli.loading

import asciiart.core.image.RGBImage

/**
 * Trait defining a strategy for loading images.
 *
 * Specifies a contract for implementing image loading strategies.
 */
trait ImageLoadingStrategy {

  /**
   * Loads an image and returns it as an `RGBImage`.
   *
   * @return The loaded image as an `RGBImage`.
   */
  def load(): RGBImage
}
