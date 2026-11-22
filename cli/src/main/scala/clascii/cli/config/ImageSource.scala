package clascii.cli.config


/**
 * Represents the source of an image for ASCII art generation.
 *
 * This sealed trait is the base for defining different types of image sources,
 * such as from a file path or a randomly generated image.
 */
sealed trait ImageSource

/**
 * Represents an image source from a specified file path.
 *
 * This case class is used when the source of the ASCII art is an image file located at a specific path.
 *
 * @param path the file path of the image.
 */
case class PathImageSource(path: String) extends ImageSource

/**
 * A generated picture (a plasma) instead of a file.
 *
 * @param seed Draws the same picture every time; without it each run draws a new one.
 */
case class RandomImageSource(seed: Option[Long] = None) extends ImageSource