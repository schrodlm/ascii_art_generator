package asciiart.core.pipeline

import asciiart.core.conversion.{GrayscaleToAsciiConverter, RgbToGrayscaleConverter}
import asciiart.core.filter.{Filter, ResizeFilter}
import asciiart.core.image.{AsciiArt, Image, RGBImage}

/**
 * Turns an already-loaded image into ASCII art.
 *
 * Loading the image and showing the result are left to the caller.
 */
object AsciiArtPipeline {

  def render(image: RGBImage, settings: RenderSettings): AsciiArt = {
    val rgb = applyFilters(image, settings.rgbFilters)
    val columns = fitColumns(rgb.getWidth(), rgb.getHeight(), settings)
    val resized = new ResizeFilter(columns, settings.cellAspect).apply(rgb)
    val grayscale = applyFilters(
      new RgbToGrayscaleConverter(resized).convert(),
      settings.grayscaleFilters
    )
    val characters = new GrayscaleToAsciiConverter(grayscale, settings.table).convert()
    // One colour per character: the resized image's pixels, which grayscale filters never touch.
    val colors = Option.when(settings.colors)(resized.getData())
    val asciiArt = new AsciiArt(characters.getWidth(), characters.getHeight(), characters.getData(), colors)
    applyFilters(asciiArt, settings.asciiFilters)
  }

  // rows = height / width * columns * cellAspect, solved for the columns that fit maxRows.
  private def fitColumns(width: Int, height: Int, settings: RenderSettings): Int = {
    val fitsRows = (settings.maxRows.toDouble * width / (height * settings.cellAspect)).toInt
    width min settings.maxColumns min (fitsRows max 1)
  }

  private def applyFilters[T <: Image](image: T, filters: Seq[Filter[T]]): T =
    filters.foldLeft(image)((current, filter) => filter.apply(current))
}
