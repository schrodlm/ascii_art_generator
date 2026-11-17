package Processing

import Filter.{AsciiArtFilter, GrayscaleImageFilter, RGBImageFilter}
import Config.Tables.{DefaultTable, Table}

/**
 * Everything that decides how an image is rendered, independent of where the
 * image comes from or where the result goes. Filters run in the given order
 * within their stage.
 */
final case class RenderSettings(
  table: Table = new DefaultTable(),
  rgbFilters: Seq[RGBImageFilter] = Nil,
  grayscaleFilters: Seq[GrayscaleImageFilter] = Nil,
  asciiFilters: Seq[AsciiArtFilter] = Nil
)
