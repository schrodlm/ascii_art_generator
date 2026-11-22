package asciiart.core.pipeline

import asciiart.core.conversion.{CharacterConverter, TableConverter}
import asciiart.core.filter.{AsciiArtFilter, GrayscaleImageFilter, RGBImageFilter}
import asciiart.core.table.DefaultTable

/**
 * Everything that decides how an image is rendered, independent of where the
 * image comes from or where the result goes. Filters run in the given order
 * within their stage.
 *
 * @param characterConverter How grey pixels become characters: a TableConverter with any
 *                           table, or a converter of your own.
 * @param maxColumns Most characters per line. The output fits inside maxColumns by maxRows,
 *                   whichever limit is tighter, and is never wider than the image.
 * @param maxRows    Most lines.
 * @param cellAspect Width of a character cell divided by its height. 0.5 suits
 *                   monospace fonts 0.6 em wide on lines 1.2 em tall.
 * @param colors     Whether the art keeps each character's colour: the average colour of the
 *                   pixels it covers. Grayscale filters change the characters, not the colours.
 */
final case class RenderSettings(
  characterConverter: CharacterConverter = new TableConverter(new DefaultTable()),
  maxColumns: Int = Int.MaxValue,
  maxRows: Int = Int.MaxValue,
  cellAspect: Double = 0.5,
  rgbFilters: Seq[RGBImageFilter] = Nil,
  grayscaleFilters: Seq[GrayscaleImageFilter] = Nil,
  asciiFilters: Seq[AsciiArtFilter] = Nil,
  colors: Boolean = false
)
