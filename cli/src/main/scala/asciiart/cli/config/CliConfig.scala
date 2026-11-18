package asciiart.cli.config

import asciiart.core.filter.{AsciiArtFilter, GrayscaleImageFilter, RGBImageFilter}
import asciiart.core.pipeline.RenderSettings
import asciiart.core.table.{CustomTable, TableNameMapper}

/**
 * What the CLI was asked to do: where the image comes from, where the art
 * goes, and how to render it.
 */
final case class CliConfig(
  imageSource: ImageSource,
  imageOutput: ImageOutput,
  render: RenderSettings
)

object CliConfig {

  /** Fluent builder used by the command line parser. */
  class Builder {
    private var imageSource: Option[ImageSource] = None
    private var imageOutput: Option[ImageOutput] = None
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
      CliConfig(source, output, render)
    }
  }
}
