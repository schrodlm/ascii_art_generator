package asciiart.jsapi

import asciiart.core.image.{AsciiArt, RGBImage}

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.typedarray.Uint8ClampedArray

/** Turns core's results into plain JavaScript values. */
private[jsapi] object ToJs {

  /** The art as { columns, rows, lines, text }, plus colors when the art has them. */
  def art(art: AsciiArt): js.Object = {
    val lines = art.getData().grouped(art.getWidth()).map(_.map(_.toChar).mkString).toJSArray
    val result = js.Dynamic.literal(
      columns = art.getWidth(), rows = art.getHeight(), lines = lines, text = lines.join("\n"))
    for (colors <- art.colors) result.colors = rgba(colors)
    result
  }

  /** Core's image in the browser's layout, as { data, width, height }. */
  def pixels(image: RGBImage): js.Object =
    js.Dynamic.literal(data = rgba(image.getData()), width = image.getWidth(), height = image.getHeight())

  /** Core's 0xAARRGGBB numbers as the browser's red, green, blue and alpha bytes. */
  private def rgba(argb: Array[Int]): Uint8ClampedArray = {
    val bytes = new Uint8ClampedArray(argb.length * 4)
    for (i <- argb.indices) {
      val pixel = argb(i)
      val j = i * 4
      bytes(j) = (pixel >> 16) & 0xFF
      bytes(j + 1) = (pixel >> 8) & 0xFF
      bytes(j + 2) = pixel & 0xFF
      bytes(j + 3) = pixel >>> 24
    }
    bytes
  }
}
