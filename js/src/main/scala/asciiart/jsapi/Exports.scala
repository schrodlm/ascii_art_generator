package asciiart.jsapi

import asciiart.core.pipeline.AsciiArtPipeline
import asciiart.core.table.TableNameMapper

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExportTopLevel

/** What the JS library offers. Only members marked @JSExportTopLevel are visible to JavaScript. */
object Exports {

  /** The names of the built-in tables. Frozen, so a caller cannot change the list. */
  @JSExportTopLevel("tables")
  val tables: js.Array[String] = js.Object.freeze(TableNameMapper.names.toJSArray)

  /**
   * Turns pixels into ASCII art, returned as { columns, rows, lines, text }.
   *
   * @param pixels   { data, width, height }, data holding red, green, blue and alpha for every
   *                 pixel, row by row, such as a canvas's ImageData.
   * @param settings Optional, every key too: maxColumns and maxRows (the art fits inside both);
   *                 cellAspect (a character cell's width divided by its height); table (a built-in
   *                 table's name, { chars } darkest first, or { limits: [[grey, char], …] });
   *                 rgbFilters (rotate), greyFilters (brightness, contrast, invert) and asciiFilters
   *                 (flip, scale), each a list of { type, … } run in order.
   */
  @JSExportTopLevel("render")
  def render(pixels: Any, settings: Any = js.undefined): js.Object =
    ToJs.art(AsciiArtPipeline.render(FromJs.image(pixels), FromJs.settings(settings)))

}
