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
      |Usage: asciiart [options] [source]
      |
      |Mandatory Options:
      |  One of the following image source options must be specified:
      |    --image-random                 Use a random image as the source.
      |    --image [path]                 Specify the path of the image file to use as the source.
      |
      |  One of the following output options must be specified:
      |    --output-console               Output the ASCII art to the console.
      |    --output-file [path]           Output the ASCII art to a file at the specified path.
      |
      |Optional Options:
      |  --table [name]                 Specify the name of the predefined character table ('default', 'mathematical', 'nonlinear-default').
      |  --custom-table [chars]         Define a custom character table.
      |  --rotate [degrees]             Rotate clockwise by a multiple of 90 degrees (negative turns anticlockwise).
      |  --scale [factor]               Scale the output by a factor (2 doubles it, 0.5 halves it).
      |  --invert                       Invert brightness, so black becomes white (for dark terminals).
      |  --flip [axis]                  Flip the image along a specified axis ('horizontal' or 'vertical').! NOT IMPLEMENTED YET !
      |  --brightness [value]           Adjust the brightness of the image (value must be an integer).
      |  --contrast [factor]            Stretch brightness away from mid-grey (2 doubles the contrast, 0.5 halves it).
      |  --width [columns]              Fit the image into at most this many columns before --scale (a smaller image is not
      |                                 enlarged). Default: the terminal's width with --output-console, else 100.
      |  --font-aspect-ratio [x:y]      Width to height of one character on screen (default 1:2; 1:1 turns the correction off).
      |
      |Examples:
      |  asciiart --image /path/to/image.jpg --rotate 90 --output-console
      |  asciiart --image-random --brightness 5 --output-file /path/to/output.txt
      |
      |Note:
      |  Some options might require additional arguments.
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
