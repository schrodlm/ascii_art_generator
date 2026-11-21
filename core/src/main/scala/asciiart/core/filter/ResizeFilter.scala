package asciiart.core.filter

import asciiart.core.image.RGBImage

/**
 * Resizes an RGB image to a number of columns. Each output pixel is the average
 * of the source pixels it covers. The number of rows keeps the picture's
 * proportions when every pixel is drawn as one character cell.
 *
 * @param columns    Width of the output, one character per column.
 * @param cellAspect Width of a character cell divided by its height; 1 means square cells.
 */
private[core] class ResizeFilter(val columns: Int, val cellAspect: Double) extends RGBImageFilter {

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
    if (rows > Int.MaxValue / columns) throw new IllegalArgumentException("Output would be too large")
    val newHeight = rows.toInt
    val data = image.getData()
    val newData = new Array[Int](columns * newHeight)

    // Plain loops rather than collections: this runs on every render and visits every source pixel.
    var outY = 0
    while (outY < newHeight) {
      val y0 = (outY.toLong * height / newHeight).toInt
      val y1 = math.max(y0 + 1, ((outY + 1).toLong * height / newHeight).toInt)
      var outX = 0
      while (outX < columns) {
        val x0 = (outX.toLong * width / columns).toInt
        val x1 = math.max(x0 + 1, ((outX + 1).toLong * width / columns).toInt)

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
        newData(outY * columns + outX) =
          0xFF000000 | ((red / count).toInt << 16) | ((green / count).toInt << 8) | (blue / count).toInt
        outX += 1
      }
      outY += 1
    }

    new RGBImage(columns, newHeight, newData)
  }
}
