package clascii.core.conversion

import clascii.core.table.Table
import clascii.core.image.{AsciiArt, GrayscaleImage}

/**
 * Converts a GrayscaleImage to AsciiArt, one character for every grey pixel.
 *
 * @param table The character table to use for conversion.
 */
class TableConverter(val table: Table) extends CharacterConverter {

  /**
   * Converts the GrayscaleImage to AsciiArt.
   *
   * Maps grayscale values to ASCII characters based on the provided table.
   *
   * @param image The GrayscaleImage to convert.
   * @return The converted AsciiArt.
   */
  def convert(image: GrayscaleImage): AsciiArt = {

    val ascii_data = image.getData().map { value =>
      val grayscaleValue = image.getGrayscale(value)
      table.getChar(grayscaleValue).toInt
    }

    new AsciiArt(image.getWidth(), image.getHeight(), ascii_data)
  }
}
