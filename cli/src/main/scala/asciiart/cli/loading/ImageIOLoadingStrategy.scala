package asciiart.cli.loading

import asciiart.core.image.RGBImage

import java.awt.image.BufferedImage
import java.io.{File, IOException}
import javax.imageio.ImageIO
/**
 * Loads images using ImageIO and converts them.
 *
 * @param path Path to the image file.
 * @param converter Converts BufferedImage to RGBImage.
 */
class ImageIOLoadingStrategy(path: String, converter: BufferedImage => RGBImage) extends ImageLoadingStrategy {

  /**
   * Loads and converts an image from the specified path.
   *
   * @return Converted RGBImage.
   */
  override def load(): RGBImage = {
    val bufferedImage = readImage(path)
    converter(bufferedImage)
  }

  /**
   * Reads an image from a file path. ImageIO recognises the format from the
   * file's contents, so the extension does not matter.
   *
   * @param path Path to the image file.
   * @return BufferedImage read from the file.
   * @throws IllegalArgumentException if the file cannot be read or is not a supported image.
   */
  protected def readImage(path: String): BufferedImage = {
    val file = new File(path)
    if (!file.isFile || !file.canRead) {
      throw new IllegalArgumentException(s"Cannot read the file '$path'")
    }
    val image = try ImageIO.read(file) catch {
      case e: IOException => throw new IllegalArgumentException(s"'$path' is not a valid image: ${e.getMessage}")
    }
    if (image == null) {
      throw new IllegalArgumentException(s"'$path' is not an image in a supported format (PNG, JPEG, GIF, BMP or TIFF)")
    }
    image
  }
}
