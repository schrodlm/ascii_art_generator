package asciiart.cli.parser

import asciiart.cli.config.{CliConfig, ConsoleImageOutput, PathImageOutput, PathImageSource, RandomImageSource}
import asciiart.core.filter.{BrightnessFilter, ContrastFilter, FlipFilter, InvertFilter, RotationFilter, ScaleFilter}

import scala.util.Try


/**
 * A command line parser for configuring ASCII art generation.
 *
 * This parser interprets command line arguments to configure various aspects of ASCII art generation,
 * including image sources, filters, and output options.
 *
 * @param args the command line arguments
 */
class CommandLineParser(val args: Array[String]) extends Parser {

  val usage: String =
    """
      |Usage: asciiart (--image <path> | --image-random)
      |                (--output-console | --output-file <path>) [options]
      |
      |Turns an image into ASCII art.
      |
      |Source, pick --image or --image-random:
      |  --image <path>               A PNG, JPEG, GIF, BMP or TIFF file.
      |  --image-random               A generated picture (a plasma), different every
      |                               run.
      |  --seed <number>              With --image-random: draw the same picture every
      |                               time.
      |
      |Output, pick one:
      |  --output-console             Print the art.
      |  --output-file <path>         Write the art to a file.
      |
      |Size:
      |  --width <columns>            Fit the image into at most this many columns (a
      |                               smaller image is not enlarged). Default: the
      |                               terminal's width with --output-console, else 100.
      |  --font-aspect-ratio <x:y>    Width to height of one character, so the art
      |                               keeps its proportions (default 1:2; 1:1 turns
      |                               this off).
      |  --rotate <degrees>           Rotate clockwise by a multiple of 90 degrees
      |                               (negative turns anticlockwise).
      |
      |Brightness, on a scale of 0 (black) to 255 (white):
      |  --brightness <amount>        Add this to every value (negative darkens).
      |  --contrast <factor>          Stretch values away from mid-grey (2 doubles the
      |                               contrast, 0.5 halves it).
      |  --invert                     Swap black and white (for dark terminals).
      |
      |Characters:
      |  --table <name>               A built-in table: default, nonlinear-default or
      |                               paulborkes.
      |  --custom-table <chars>       Your own characters, darkest first, for example
      |                               "@#+-. ".
      |  --flip <axis>                vertical (or x) turns the art upside down;
      |                               horizontal (or y) mirrors it.
      |  --scale <factor>             Scale the finished art (2 doubles it, 0.5 halves
      |                               it).
      |
      |  --help                       Show this text.
      |
      |The options apply in this order: --rotate, --width, the brightness options,
      |--table or --custom-table, --flip, --scale. Options in the same step apply in
      |the order you give them.
      |
      |Examples:
      |  asciiart --image photo.jpg --output-console
      |  asciiart --image photo.jpg --width 60 --invert --output-console
      |  asciiart --image-random --seed 7 --contrast 1.5 --output-file art.txt
   """.stripMargin

  /**
   * Parses the command line arguments to configure ASCII art generation.
   *
   * This method processes each command line argument and applies the corresponding
   * configuration to `CliConfig.Builder`.
   */
  override def parse(): Option[CliConfig] = {

    if (args.length == 0 || args.contains("--help")) {
      println(usage)
      return None
    }

    val configBuilder: CliConfig.Builder = nextArg(new CliConfig.Builder(), args.toList)
    Some(configBuilder.build())
  }

  /**
   * Recursively processes command line arguments to configure the ASCII art generator.
   *
   * @param configBuilder the current state of the ASCII art configuration builder
   * @param list          the remaining command line arguments to process
   * @return the updated configuration builder
   */
  def nextArg(configBuilder: CliConfig.Builder, list: List[String]): CliConfig.Builder = {

    list match {
      case Nil => configBuilder

      case "--image-random" :: tail =>
        nextArg(configBuilder.withImageSource(RandomImageSource()), tail)
      case "--seed" :: value :: tail =>
        val seed = Try(value.toLong).getOrElse(throw new IllegalArgumentException("Seed must be a whole number"))
        nextArg(configBuilder.withSeed(seed), tail)
      case "--image" :: path :: tail =>
        nextArg(configBuilder.withImageSource(PathImageSource(path)), tail)
      case "--table" :: name :: tail =>
        nextArg(configBuilder.withTable(name), tail)
      case "--custom-table" :: chars :: tail =>
        val table_chars: Array[Char] = chars.toCharArray
        nextArg(configBuilder.withTable(table_chars), tail)
      case "--rotate" :: degrees :: tail =>
        val rotationValue = Try(degrees.toInt).getOrElse(throw new IllegalArgumentException("Rotation value must be a whole number"))
        val rotationFilter: RotationFilter = new RotationFilter(rotationValue)
        nextArg(configBuilder.addRgbFilter(rotationFilter), tail)
      case "--scale" :: value :: tail =>
        val scaleValue = Try(value.toDouble).getOrElse(throw new IllegalArgumentException("Scale value must be a valid number"))
        val scaleFilter: ScaleFilter = new ScaleFilter(scaleValue)
        nextArg(configBuilder.addAsciiFilter(scaleFilter), tail)
      case "--invert" :: tail =>
        val invertFilter: InvertFilter = new InvertFilter()
        nextArg(configBuilder.addGrayscaleFilter(invertFilter), tail)
      case "--flip" :: axis :: tail =>
        val flipFilter: FlipFilter = new FlipFilter(axis)
        nextArg(configBuilder.addAsciiFilter(flipFilter), tail)
      case "--brightness" :: value :: tail =>
        val brightnessValue = Try(value.toInt).getOrElse(throw new IllegalArgumentException("Brightness value must be a valid number"))
        val brightnessFilter: BrightnessFilter = new BrightnessFilter(brightnessValue)
        nextArg(configBuilder.addGrayscaleFilter(brightnessFilter), tail)
      case "--contrast" :: value :: tail =>
        val contrastValue = Try(value.toDouble).getOrElse(throw new IllegalArgumentException("Contrast value must be a valid number"))
        val contrastFilter: ContrastFilter = new ContrastFilter(contrastValue)
        nextArg(configBuilder.addGrayscaleFilter(contrastFilter), tail)
      case "--width" :: value :: tail =>
        val columns = Try(value.toInt).filter(_ >= 1)
          .getOrElse(throw new IllegalArgumentException("Width must be a whole number of at least 1"))
        nextArg(configBuilder.withWidth(columns), tail)
      case "--font-aspect-ratio" :: ratio :: tail =>
        parseFontAspectRatio(ratio) match {
          case Some((x, y)) if x > 0 && y > 0 && x <= 10L * y && y <= 10L * x =>
            nextArg(configBuilder.withCellAspect(x.toDouble / y), tail)
          case _ =>
            throw new IllegalArgumentException("Font aspect ratio must be two positive whole numbers between 1:10 and 10:1, like 1:2")
        }
      case "--output-console" :: tail =>
        nextArg(configBuilder.withImageOutput(ConsoleImageOutput()), tail)
      case "--output-file" :: path :: tail =>
        nextArg(configBuilder.withImageOutput(PathImageOutput(path)), tail)

      case unknown :: _ =>
        throw new IllegalArgumentException("Unknown option " + unknown)
    }
  }

  /**
   * Parses a string representing font aspect ratio in the format "x:y".
   *
   * @param input the input string to parse
   * @return an Option containing a tuple (Int, Int) if parsing is successful, None otherwise
   */
  def parseFontAspectRatio(input: String): Option[(Int, Int)] = {
    input.split(":", -1) match {
      case Array(a, b) =>
        try {
          Some((a.toInt, b.toInt))
        } catch {
          case _: NumberFormatException => None
        }
      case _ => None
    }
  }

}
