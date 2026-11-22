package clascii.jsapi

import clascii.core.characters.Dither
import clascii.core.conversion.{BrailleConverter, CharacterConverter, TableConverter}
import clascii.core.filter._
import clascii.core.image.RGBImage
import clascii.core.pipeline.RenderSettings
import clascii.core.table.{CustomTable, NonLinearTable, TableNameMapper}

import scala.scalajs.js
import scala.scalajs.js.typedarray.{Uint8Array, Uint8ClampedArray}
import scala.util.Random

/**
 * Turns values from JavaScript into core's types. JavaScript can pass anything,
 * and a production build does not check JS values against Scala types, so every
 * value arrives as `Any` and is checked here before core sees it. Problems throw
 * IllegalArgumentException, which JavaScript receives as an Error.
 */
private[jsapi] object FromJs {

  /** Pixels in the browser's layout (red, green, blue, alpha per pixel) as core's image. */
  def image(value: Any): RGBImage = {
    // Other keys are allowed, so an ImageData, or any object carrying more, can be passed.
    val fields = record(value, "pixels must be an object with data, width and height")
    val width = positiveWholeNumber(fields.width, "pixels.width")
    val height = positiveWholeNumber(fields.height, "pixels.height")
    val data = bytes(fields.data, "pixels.data")
    // In Long, so a huge width and height cannot overflow.
    val expected = width.toLong * height * 4
    if (expected > Int.MaxValue) fail(s"pixels is too large: at most ${Int.MaxValue} values")
    if (data.length != expected) fail(s"pixels.data must hold width × height × 4 = $expected values, not ${data.length}")

    // One number per pixel, 0xAARRGGBB, which is how core stores colour.
    val argb = new Array[Int](width * height)
    for (i <- argb.indices) {
      val j = i * 4
      argb(i) = (data(j + 3) << 24) | (data(j) << 16) | (data(j + 1) << 8) | data(j + 2)
    }
    new RGBImage(width, height, argb)
  }

  /** The settings object; every key is optional, and undefined means all defaults. */
  def settings(value: Any): RenderSettings =
    if (js.isUndefined(value)) RenderSettings()
    else {
      val fields = record(value, "settings must be an object")
      onlyKeys(fields, "settings",
        Set("maxColumns", "maxRows", "cellAspect", "characters", "rgbFilters", "greyFilters", "asciiFilters", "colors"))
      val defaults = RenderSettings()
      RenderSettings(
        characterConverter = orDefault(fields.characters, defaults.characterConverter)(characterConverter),
        maxColumns = orDefault(fields.maxColumns, defaults.maxColumns)(positiveWholeNumber(_, "maxColumns")),
        maxRows = orDefault(fields.maxRows, defaults.maxRows)(positiveWholeNumber(_, "maxRows")),
        cellAspect = orDefault(fields.cellAspect, defaults.cellAspect)(number(_, "cellAspect")),
        rgbFilters = orDefault(fields.rgbFilters, defaults.rgbFilters)(filters(_, "rgbFilters", rgbTypes)),
        grayscaleFilters = orDefault(fields.greyFilters, defaults.grayscaleFilters)(filters(_, "greyFilters", greyTypes)),
        asciiFilters = orDefault(fields.asciiFilters, defaults.asciiFilters)(filters(_, "asciiFilters", asciiTypes)),
        colors = orDefault(fields.colors, defaults.colors)(boolean(_, "colors"))
      )
    }

  def seed(value: Any): Long = value match {
    case _ if js.isUndefined(value) => Random.nextLong()
    // Beyond 2^53 a JS number cannot hold every whole number, so larger seeds would not repeat.
    case n: Double if n == math.floor(n) && math.abs(n) <= 9007199254740991.0 => n.toLong
    case _ => fail("seed must be a whole number between -(2^53 - 1) and 2^53 - 1")
  }

  private def characterConverter(value: Any): CharacterConverter = value match {
    case name: String => new TableConverter(TableNameMapper(name))
    case _ =>
      val shape = "characters must be a built-in table's name, { chars }, { limits } or { braille }"
      val fields = record(value, shape)
      val kinds = Seq("chars", "limits", "braille")
      onlyKeys(fields, "characters", kinds.toSet)
      // Exactly one of them.
      kinds.filterNot(kind => js.isUndefined(fields.selectDynamic(kind))) match {
        case Seq("chars") => new TableConverter(new CustomTable(characters(fields.chars, "characters.chars")))
        case Seq("limits") => new TableConverter(new NonLinearTable(limits(fields.limits)))
        case Seq("braille") => new BrailleConverter(dither(fields.braille))
        case _ => fail(shape)
      }
  }

  private def dither(value: Any): Dither = value match {
    case name: String if Dither.names.contains(name) => Dither.named(name).get
    case _ => fail(s"characters.braille must be one of ${Dither.names.mkString(", ")}")
  }

  private def limits(value: Any): Array[(Int, Char)] =
    array(value, "characters.limits").zipWithIndex.map { case (entry, i) =>
      val at = s"characters.limits[$i]"
      array(entry, at) match {
        case Seq(limit, char) => (wholeNumber(limit, s"$at[0]"), character(char, s"$at[1]"))
        case _ => fail(s"$at must be [limit, character]")
      }
    }.toArray

  // One type of filter: its `type` name, its other fields, and how to build it from them.
  private final case class FilterType[F](name: String, fields: Set[String], build: (js.Dynamic, String) => F)

  private val rgbTypes: Seq[FilterType[RGBImageFilter]] = Seq(
    FilterType("rotate", Set("degrees"), (f, at) => new RotationFilter(wholeNumber(f.degrees, s"$at.degrees"))),
    FilterType("flip", Set("direction"), (f, at) => new FlipFilter(direction(f.direction, s"$at.direction")))
  )

  private val greyTypes: Seq[FilterType[GrayscaleImageFilter]] = Seq(
    FilterType("brightness", Set("amount"), (f, at) => new BrightnessFilter(wholeNumber(f.amount, s"$at.amount"))),
    FilterType("contrast", Set("factor"), (f, at) => new ContrastFilter(number(f.factor, s"$at.factor"))),
    FilterType("invert", Set(), (_, _) => new InvertFilter())
  )

  private val asciiTypes: Seq[FilterType[AsciiArtFilter]] = Seq(
    FilterType("scale", Set("factor"), (f, at) => new ScaleFilter(number(f.factor, s"$at.factor")))
  )

  private def filters[F](value: Any, name: String, types: Seq[FilterType[F]]): Seq[F] =
    array(value, name).zipWithIndex.map { case (entry, i) =>
      val at = s"$name[$i]"
      val fields = record(entry, s"$at must be an object with a type")
      val typeName: Any = fields.selectDynamic("type")
      val filterType = types.find(_.name == typeName)
        .getOrElse(fail(s"$at.type must be one of ${types.map(_.name).mkString(", ")}"))
      onlyKeys(fields, at, filterType.fields + "type")
      filterType.build(fields, at)
    }

  private def direction(value: Any, name: String): Flip = value match {
    case "vertical" => Flip.Vertical
    case "horizontal" => Flip.Horizontal
    case _ => fail(s"""$name must be "vertical" or "horizontal"""")
  }

  /** A plain object, as js.Dynamic so its fields can be read. */
  private def record(value: Any, problem: String): js.Dynamic =
    if (js.typeOf(value) == "object" && value != null && !js.Array.isArray(value)) value.asInstanceOf[js.Dynamic]
    else fail(problem)

  /** Rejects any other key: JavaScript would otherwise ignore a misspelt one silently. */
  private def onlyKeys(fields: js.Dynamic, name: String, allowed: Set[String]): Unit =
    for (key <- js.Object.keys(fields.asInstanceOf[js.Object]) if !allowed(key)) {
      fail(s"Unknown key '$key' in $name: use ${allowed.toSeq.sorted.mkString(", ")}")
    }

  /** `read(value)`, or `default` when the key is missing. */
  private def orDefault[A](value: Any, default: A)(read: Any => A): A =
    if (js.isUndefined(value)) default else read(value)

  private def array(value: Any, name: String): Seq[Any] =
    if (js.Array.isArray(value)) value.asInstanceOf[js.Array[Any]].toSeq else fail(s"$name must be an array")

  private def bytes(value: Any, name: String): Uint8ClampedArray = value match {
    case clamped: Uint8ClampedArray => clamped
    case plain: Uint8Array => new Uint8ClampedArray(plain.buffer, plain.byteOffset, plain.length)
    case _ => fail(s"$name must be a Uint8ClampedArray or a Uint8Array")
  }

  private def boolean(value: Any, name: String): Boolean = value match {
    case b: Boolean => b
    case _ => fail(s"$name must be true or false")
  }

  private def number(value: Any, name: String): Double = value match {
    case n: Double => n
    case _ => fail(s"$name must be a number")
  }

  private def wholeNumber(value: Any, name: String): Int = value match {
    case n: Double if n == math.floor(n) && n >= Int.MinValue && n <= Int.MaxValue => n.toInt
    case _ => fail(s"$name must be a whole number")
  }

  private def positiveWholeNumber(value: Any, name: String): Int = value match {
    case n: Double if n == math.floor(n) && n >= 1 && n <= Int.MaxValue => n.toInt
    case _ => fail(s"$name must be a whole number of at least 1")
  }

  // Core works with 16-bit chars, so a character beyond U+FFFF (most emoji) would split in two.
  // Control characters such as a newline would break the art's lines.
  private def characters(value: Any, name: String): Array[Char] = value match {
    case s: String if s.exists(_.isSurrogate) => fail(s"$name cannot hold emoji or other characters beyond U+FFFF")
    case s: String if s.exists(_.isControl) => fail(s"$name cannot hold control characters such as a newline")
    case s: String => s.toCharArray
    case _ => fail(s"$name must be a string")
  }

  private def character(value: Any, name: String): Char = value match {
    case s: String if s.length == 1 && !s.head.isSurrogate && !s.head.isControl => s.head
    case _ => fail(s"$name must be a single character that is not a control character or emoji")
  }

  private def fail(message: String): Nothing = throw new IllegalArgumentException(message)
}
