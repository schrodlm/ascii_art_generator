package asciiart.core.image

/**
 * Represents ASCII art as an image.
 *
 * Extends `BaseImage` to handle ASCII art, where each pixel value corresponds to an ASCII character.
 *
 * @param width Width of the ASCII art in characters.
 * @param height Height of the ASCII art in characters.
 * @param data Array containing ASCII values representing the art. The art uses this array
 *             directly, without a copy, so don't change it afterwards.
 * @param colors Optionally, each character's colour as 0xAARRGGBB, in the same order as data.
 *               Used directly, like data.
 * @throws IllegalArgumentException if there are colours but not one for every character.
 */
class AsciiArt(width: Int, height: Int, data: Array[Int], val colors: Option[Array[Int]] = None)
  extends BaseImage(width, height, data) {

  if (colors.exists(_.length != data.length)) {
    throw new IllegalArgumentException("There must be one colour for every character")
  }

  def toText: String =
    getData().grouped(getWidth()).map(_.map(_.toChar).mkString).mkString("\n")

  /**
   * A new art of the given size whose every cell copies a cell of this one, character and colour
   * together, so filters that move characters around cannot separate them from their colours.
   *
   * @param source Given a cell (x, y) of the new art, the (x, y) of the cell it copies.
   * @throws IllegalArgumentException if source gives a cell outside this art.
   */
  def rearrange(newWidth: Int, newHeight: Int)(source: (Int, Int) => (Int, Int)): AsciiArt = {
    val width = getWidth()
    val height = getHeight()
    val sourceIndex = Array.tabulate(newWidth * newHeight) { i =>
      val (x, y) = source(i % newWidth, i / newWidth)
      if (x < 0 || x >= width || y < 0 || y >= height) {
        throw new IllegalArgumentException(s"Cell ($x, $y) is outside the $width by $height art")
      }
      y * width + x
    }
    def copy(values: Array[Int]): Array[Int] = sourceIndex.map(values(_))
    new AsciiArt(newWidth, newHeight, copy(getData()), colors.map(copy))
  }
}
