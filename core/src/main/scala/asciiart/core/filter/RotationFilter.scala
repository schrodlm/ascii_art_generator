package asciiart.core.filter
import asciiart.core.image.AsciiArt

class RotationFilter(val rotation: Int) extends AsciiArtFilter {
  override def apply(image: AsciiArt): AsciiArt = ???
}
