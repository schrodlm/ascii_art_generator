import Conversion.SourceToRgbImage
import InputParser.CommandLineParser
import Processing.AsciiArtPipeline

object Main {
  def main(args: Array[String]): Unit = {
    val parser = new CommandLineParser(args)
    val config = parser.parse().getOrElse(return)
    val image = new SourceToRgbImage(config.imageSource).convert()
    val asciiArt = AsciiArtPipeline.render(image, config.render)
    config.imageOutput.save(asciiArt)
  }
}
