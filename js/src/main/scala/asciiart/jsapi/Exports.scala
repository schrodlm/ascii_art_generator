package asciiart.jsapi

import asciiart.core.table.TableNameMapper

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel

/** What the JS library offers. Only members marked @JSExportTopLevel are visible to JavaScript. */
object Exports {

  /** The names of the built-in tables. Frozen, so a caller cannot change the list. */
  @JSExportTopLevel("tables")
  val tables: js.Array[String] = js.Object.freeze(js.Array(TableNameMapper.names: _*))
}
