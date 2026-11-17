package asciiart.core.pipeline

import asciiart.core.conversion.{GrayscaleToAsciiConverter, RgbToGrayscaleConverter}
import asciiart.core.filter.Filter
import asciiart.core.image.{AsciiArt, Image, RGBImage}

/**
 * Turns an already-loaded image into ASCII art.
 *
 * Loading the image and showing the result are left to the caller.
 */
object AsciiArtPipeline {

  def render(image: RGBImage, settings: RenderSettings): AsciiArt = {
    val rgb = applyFilters(image, settings.rgbFilters)
    val grayscale = applyFilters(
      new RgbToGrayscaleConverter(rgb).convert(),
      settings.grayscaleFilters
    )
    val asciiArt = new GrayscaleToAsciiConverter(grayscale, settings.table).convert()
    applyFilters(asciiArt, settings.asciiFilters)
  }

  private def applyFilters[T <: Image](image: T, filters: Seq[Filter[T]]): T =
    filters.foldLeft(image)((current, filter) => filter.apply(current))
}
