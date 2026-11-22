package asciiart.core.filter

import asciiart.core.image.RGBImage

/**
 * Resizes an RGB image to a number of character columns. Each output pixel is the average
 * of the source pixels it covers. The number of rows keeps the picture's proportions when
 * the output is drawn in character cells of the given shape.
 *
 * @param columns    Width of the output in characters.
 * @param cellAspect Width of a character cell divided by its height; 1 means square cells.
 * @param cellWidth  Output pixels per character, across: 1 for a table, 2 for braille.
 * @param cellHeight Output pixels per character, down: 1 for a table, 4 for braille.
 */
private[core] class ResizeFilter(val columns: Int, val cellAspect: Double, val cellWidth: Int = 1, val cellHeight: Int = 1)
  extends RGBImageFilter {

  /**
   * Applies the resize to an RGBImage.
   *
   * @param image The RGBImage to resize.
   * @return The resized RGBImage, at least one row tall.
   * @throws IllegalArgumentException if the image is empty, columns is below 1, cellAspect is not
   *                                  a finite positive number, or the output would not fit in an array.
   */
  override def apply(image: RGBImage): RGBImage = {
    val width = image.getWidth()
    val height = image.getHeight()
    if (width == 0 || height == 0) throw new IllegalArgumentException("The image is empty")
    if (columns < 1) throw new IllegalArgumentException("There must be at least 1 column")
    if (!(cellAspect > 0) || cellAspect.isInfinite) throw new IllegalArgumentException("Cell aspect must be a finite positive number")
    val rows = math.max(1L, math.round(height.toDouble / width * columns * cellAspect))
    // Divided rather than multiplied, so the check itself cannot overflow.
    if (rows > Int.MaxValue / cellHeight / (columns.toLong * cellWidth)) throw new IllegalArgumentException("Output would be too large")
    val newWidth = columns * cellWidth
    val newHeight = rows.toInt * cellHeight
    val data = image.getData()
    val newData = new Array[Int](newWidth * newHeight)

    // Plain loops rather than collections: this runs on every render and visits every source pixel.
    var outY = 0
    while (outY < newHeight) {
      val y0 = (outY.toLong * height / newHeight).toInt
      val y1 = math.max(y0 + 1, ((outY + 1).toLong * height / newHeight).toInt)
      var outX = 0
      while (outX < newWidth) {
        val x0 = (outX.toLong * width / newWidth).toInt
        val x1 = math.max(x0 + 1, ((outX + 1).toLong * width / newWidth).toInt)

        // Doubles add whole numbers exactly far beyond any image size; Long is emulated, and slow, in JavaScript.
        var red, green, blue = 0.0
        var y = y0
        while (y < y1) {
          var i = y * width + x0
          val rowEnd = y * width + x1
          while (i < rowEnd) {
            val pixel = data(i)
            red += (pixel >> 16) & 0xFF
            green += (pixel >> 8) & 0xFF
            blue += pixel & 0xFF
            i += 1
          }
          y += 1
        }
        val count = (x1 - x0) * (y1 - y0)
        newData(outY * newWidth + outX) =
          0xFF000000 | ((red / count).toInt << 16) | ((green / count).toInt << 8) | (blue / count).toInt
        outX += 1
      }
      outY += 1
    }

    new RGBImage(newWidth, newHeight, newData)
  }
}
