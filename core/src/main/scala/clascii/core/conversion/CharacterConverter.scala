package clascii.core.conversion

import clascii.core.image.{AsciiArt, GrayscaleImage}

/**
 * Turns a grey image into characters, as the last step before the ASCII art filters. A
 * TableConverter picks one character for every grey pixel; extend this to draw them another way.
 */
trait CharacterConverter {

  /** Grey pixels one character covers, across. */
  def cellWidth: Int = 1

  /** Grey pixels one character covers, down. */
  def cellHeight: Int = 1

  /** The characters for an image that holds cellWidth by cellHeight grey pixels per character. */
  def convert(image: GrayscaleImage): AsciiArt
}
