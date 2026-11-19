package asciiart.cli

import asciiart.cli.loading.SourceToRgbImage
import asciiart.cli.parser.CommandLineParser
import asciiart.core.pipeline.AsciiArtPipeline

object Main {
  def main(args: Array[String]): Unit = {
    val status = run(args)
    if (status != 0) sys.exit(status)
  }

  /** Runs the CLI and returns its exit status. A user error prints its message, not a stack trace. */
  def run(args: Array[String]): Int =
    try {
      new CommandLineParser(args).parse() match {
        case None => 0 // usage was printed
        case Some(config) =>
          val image = new SourceToRgbImage(config.imageSource).convert()
          val asciiArt = AsciiArtPipeline.render(image, config.renderSettings(Terminal.columns()))
          config.imageOutput.save(asciiArt)
          0
      }
    } catch {
      case e: IllegalArgumentException =>
        Console.err.println(s"Error: ${e.getMessage}")
        1
    }
}
