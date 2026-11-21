package asciiart.cli.config

import asciiart.core.filter.{AsciiArtFilter, GrayscaleImageFilter, RGBImageFilter}
import asciiart.core.pipeline.RenderSettings
import asciiart.core.table.{CustomTable, TableNameMapper}

/**
 * What the CLI was asked to do: where the image comes from, where the art
 * goes, and how to render it.
 *
 * @param width The --width the user asked for, if any.
 * @param render Everything but the width, which renderSettings decides.
 */
final case class CliConfig(
  imageSource: ImageSource,
  imageOutput: ImageOutput,
  width: Option[Int],
  render: RenderSettings
) {

  /**
   * The render settings with the output width decided: the requested width,
   * else the terminal's width when printing to it, else 100 columns.
   *
   * @param terminalColumns The terminal's width; only asked for when it is needed.
   */
  def renderSettings(terminalColumns: => Option[Int]): RenderSettings = {
    val fromTerminal = imageOutput match {
      case ConsoleImageOutput() => terminalColumns
      case _ => None
    }
    render.copy(maxColumns = width.orElse(fromTerminal).getOrElse(100))
  }
}

object CliConfig {

  /** Fluent builder used by the command line parser. */
  class Builder {
    private var imageSource: Option[ImageSource] = None
    private var imageOutput: Option[ImageOutput] = None
    private var width: Option[Int] = None
    private var seed: Option[Long] = None
    private var render: RenderSettings = RenderSettings()

    def withImageSource(source: ImageSource): Builder = {
      if (imageSource.isDefined) {
        throw new IllegalArgumentException("Provided more than one image argument")
      }
      imageSource = Some(source)
      this
    }

    def withImageOutput(output: ImageOutput): Builder = {
      imageOutput = Some(output)
      this
    }

    def withTable(name: String): Builder = {
      render = render.copy(table = TableNameMapper(name))
      this
    }

    def withTable(chars: Array[Char]): Builder = {
      render = render.copy(table = new CustomTable(chars))
      this
    }

    def withSeed(seed: Long): Builder = {
      this.seed = Some(seed)
      this
    }

    def withWidth(columns: Int): Builder = {
      width = Some(columns)
      this
    }

    def withColors(): Builder = {
      render = render.copy(colors = true)
      this
    }

    def withCellAspect(cellAspect: Double): Builder = {
      render = render.copy(cellAspect = cellAspect)
      this
    }

    def addRgbFilter(filter: RGBImageFilter): Builder = {
      render = render.copy(rgbFilters = render.rgbFilters :+ filter)
      this
    }

    def addGrayscaleFilter(filter: GrayscaleImageFilter): Builder = {
      render = render.copy(grayscaleFilters = render.grayscaleFilters :+ filter)
      this
    }

    def addAsciiFilter(filter: AsciiArtFilter): Builder = {
      render = render.copy(asciiFilters = render.asciiFilters :+ filter)
      this
    }

    def build(): CliConfig = {
      val source = imageSource.getOrElse(
        throw new IllegalArgumentException("Missing an image source: use --image [path] or --image-random"))
      val output = imageOutput.getOrElse(
        throw new IllegalArgumentException("Missing an output: use --output-console or --output-file [path]"))
      val seededSource = (source, seed) match {
        case (_, None) => source
        case (_: RandomImageSource, Some(_)) => RandomImageSource(seed)
        case _ => throw new IllegalArgumentException("--seed only works with --image-random")
      }
      CliConfig(seededSource, output, width, render)
    }
  }
}
