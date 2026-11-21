package asciiart.core.filter

import asciiart.core.image.AsciiArt


/**
 * Flips an AsciiArt image.
 *
 * @param flip The direction to flip the image in.
 */
class FlipFilter(val flip: Flip) extends AsciiArtFilter {

  /**
   * Applies the flip filter to an AsciiArt image.
   *
   * @param image The AsciiArt image to flip.
   * @return The flipped AsciiArt image.
   */
  override def apply(image: AsciiArt): AsciiArt = flip match {
    case Flip.Vertical => flipVertically(image)
    case Flip.Horizontal => flipHorizontally(image)
  }

  // Flips the image vertically.
  private def flipVertically(image: AsciiArt): AsciiArt = {
    val newData = image.getData().grouped(image.getWidth()).toArray.reverse.flatten
    new AsciiArt(image.getWidth(), image.getHeight(), newData)
  }

  // Flips the image horizontally.
  private def flipHorizontally(image: AsciiArt): AsciiArt = {
    val newData = image.getData().grouped(image.getWidth()).flatMap(_.reverse).toArray
    new AsciiArt(image.getWidth(), image.getHeight(), newData)
  }
}
