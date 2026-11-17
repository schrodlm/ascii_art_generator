package Config

import Filter.{AsciiArtFilter, Filter, GrayscaleImageFilter, RGBImageFilter}
import Image.Image
import Processing.RenderSettings
import Config.Tables.{CustomTable, TableNameMapper}

/**
 * What the CLI was asked to do: where the image comes from, where the art
 * goes, and how to render it.
 */
final case class AsciiArtConfig(
  imageSource: ImageSource,
  imageOutput: ImageOutput,
  render: RenderSettings
)

object AsciiArtConfig {

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

    def addFilter(filter: Filter[_ <: Image]): Builder = {
      render = filter match {
        case f: AsciiArtFilter => render.copy(asciiFilters = render.asciiFilters :+ f)
        case f: GrayscaleImageFilter => render.copy(grayscaleFilters = render.grayscaleFilters :+ f)
        case f: RGBImageFilter => render.copy(rgbFilters = render.rgbFilters :+ f)
        case _ => render
      }
      this
    }

    def build(): AsciiArtConfig = {
      if (imageSource.isEmpty || imageOutput.isEmpty) {
        throw new IllegalStateException("Missing required fields")
      }
      AsciiArtConfig(imageSource.get, imageOutput.get, render)
    }
  }
}
