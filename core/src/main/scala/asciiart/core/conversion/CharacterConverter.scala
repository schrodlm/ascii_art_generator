package asciiart.core.conversion

import asciiart.core.image.{AsciiArt, GrayscaleImage}

/**
 * Turns a grey image into characters, as the last step before the ASCII art filters. A
 * TableConverter picks one character for every grey pixel; extend this to draw them another way.
 */
trait CharacterConverter {

  /** The characters for this grey image. */
  def convert(image: GrayscaleImage): AsciiArt
}
