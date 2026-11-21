package asciiart.web

import asciiart.core.filter.{BrightnessFilter, Flip, FlipFilter, InvertFilter, RotationFilter, ScaleFilter}
import asciiart.core.image.RGBImage
import asciiart.core.pipeline.{AsciiArtPipeline, RenderSettings}
import asciiart.core.table.{CustomTable, TableNameMapper}
import org.scalajs.dom

import scala.util.Try

// Temporary spike entry point: reaches every table and filter so the linked
// bundle is as large as v1 will need. Replaced by the real UI.
object Main {
  def main(args: Array[String]): Unit = {
    val w = 64
    val h = 32
    val pixels = Array.tabulate(w * h) { i =>
      val x = i % w
      val y = i / w
      (0xFF << 24) | ((x * 4) << 16) | ((y * 8) << 8) | ((x + y) * 2)
    }
    val image = new RGBImage(w, h, pixels)
    val tables = TableNameMapper.names.map(TableNameMapper(_)) :+
      new CustomTable(" .:-=+*#%@".toCharArray)
    val settings = tables.map { table =>
      RenderSettings(
        table = table,
        rgbFilters = Seq(new RotationFilter(90)),
        grayscaleFilters = Seq(new BrightnessFilter(10), new InvertFilter()),
        asciiFilters = Seq(new FlipFilter(Flip.Vertical), new FlipFilter(Flip.Horizontal), new ScaleFilter(0.5),
          new ScaleFilter(2))
      )
    }
    val text = settings.map { s =>
      Try(AsciiArtPipeline.render(image, s))
        .orElse(Try(AsciiArtPipeline.render(image, s.copy(grayscaleFilters = Seq(new BrightnessFilter(10)),
          asciiFilters = s.asciiFilters.take(4)))))
        .map(_.toText)
        .getOrElse("")
    }.mkString("\n\n")
    val pre = dom.document.createElement("pre")
    pre.textContent = text
    dom.document.body.appendChild(pre)
  }
}
