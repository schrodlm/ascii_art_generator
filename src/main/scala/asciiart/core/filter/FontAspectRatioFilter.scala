package asciiart.core.filter
import asciiart.core.image.{AsciiArt}

class FontAspectRatioFilter(val x : Int, val y : Int) extends AsciiArtFilter {
  override def apply(image: AsciiArt): AsciiArt = ???
}
