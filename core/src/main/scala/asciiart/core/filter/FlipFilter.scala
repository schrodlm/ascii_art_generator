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

  private def flipVertically(image: AsciiArt): AsciiArt = {
    val height = image.getHeight()
    image.rearrange(image.getWidth(), height)((x, y) => (x, height - 1 - y))
  }

  private def flipHorizontally(image: AsciiArt): AsciiArt = {
    val width = image.getWidth()
    image.rearrange(width, image.getHeight())((x, y) => (width - 1 - x, y))
  }
}
