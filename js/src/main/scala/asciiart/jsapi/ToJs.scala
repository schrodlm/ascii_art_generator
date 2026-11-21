package asciiart.jsapi

import asciiart.core.image.{AsciiArt, RGBImage}

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.typedarray.Uint8ClampedArray

/** Turns core's results into plain JavaScript values. */
private[jsapi] object ToJs {

  /** The art as { columns, rows, lines, text }. */
  def art(art: AsciiArt): js.Object = {
    val lines = art.getData().grouped(art.getWidth()).map(_.map(_.toChar).mkString).toJSArray
    js.Dynamic.literal(columns = art.getWidth(), rows = art.getHeight(), lines = lines, text = lines.join("\n"))
  }

  /** Core's image in the browser's layout, as { data, width, height }. */
  def pixels(image: RGBImage): js.Object = {
    val argb = image.getData()
    val data = new Uint8ClampedArray(argb.length * 4)
    for (i <- argb.indices) {
      val pixel = argb(i)
      val j = i * 4
      data(j) = (pixel >> 16) & 0xFF
      data(j + 1) = (pixel >> 8) & 0xFF
      data(j + 2) = pixel & 0xFF
      data(j + 3) = (pixel >>> 24) & 0xFF
    }
    js.Dynamic.literal(data = data, width = image.getWidth(), height = image.getHeight())
  }
}
