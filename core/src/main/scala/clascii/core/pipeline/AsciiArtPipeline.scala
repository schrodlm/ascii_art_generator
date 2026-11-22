package clascii.core.pipeline

import clascii.core.conversion.RgbToGrayscaleConverter
import clascii.core.filter.{Filter, ResizeFilter}
import clascii.core.image.{AsciiArt, Image, RGBImage}

/**
 * Turns an already-loaded image into ASCII art.
 *
 * Loading the image and showing the result are left to the caller.
 */
object AsciiArtPipeline {

  def render(image: RGBImage, settings: RenderSettings): AsciiArt = {
    val rgb = applyFilters(image, settings.rgbFilters)
    val characterConverter = settings.characterConverter
    val columns = fitColumns(rgb.getWidth(), rgb.getHeight(), settings)
    val resized =
      new ResizeFilter(columns, settings.cellAspect, characterConverter.cellWidth, characterConverter.cellHeight).apply(rgb)
    val grayscale = applyFilters(
      new RgbToGrayscaleConverter().convert(resized),
      settings.grayscaleFilters
    )
    val drawn = characterConverter.convert(grayscale)
    // One colour per character: the average of the pixels it covers, which grayscale filters never touch.
    val colors = Option.when(settings.colors)(
      if (characterConverter.cellWidth == 1 && characterConverter.cellHeight == 1) resized.getData()
      else new ResizeFilter(columns, settings.cellAspect).apply(rgb).getData()
    )
    val asciiArt = new AsciiArt(drawn.getWidth(), drawn.getHeight(), drawn.getData(), colors)
    applyFilters(asciiArt, settings.asciiFilters)
  }

  // rows = height / width * columns * cellAspect, solved for the columns that fit maxRows. Never more
  // columns than the image has pixels for, at cellWidth pixels per column.
  private def fitColumns(width: Int, height: Int, settings: RenderSettings): Int = {
    val fitsRows = (settings.maxRows.toDouble * width / (height * settings.cellAspect)).toInt
    (width / settings.characterConverter.cellWidth max 1) min settings.maxColumns min (fitsRows max 1)
  }

  private def applyFilters[T <: Image](image: T, filters: Seq[Filter[T]]): T =
    filters.foldLeft(image)((current, filter) => filter.apply(current))
}
