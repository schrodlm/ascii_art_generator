package asciiart.jsapi

import asciiart.core.image.AsciiArt

import scala.scalajs.js
import scala.scalajs.js.JSConverters._

/** Turns core's results into plain JavaScript values. */
private[jsapi] object ToJs {

  /** The art as { columns, rows, lines, text }. */
  def art(art: AsciiArt): js.Object = {
    val lines = art.getData().grouped(art.getWidth()).map(_.map(_.toChar).mkString).toJSArray
    js.Dynamic.literal(columns = art.getWidth(), rows = art.getHeight(), lines = lines, text = lines.join("\n"))
  }

}
