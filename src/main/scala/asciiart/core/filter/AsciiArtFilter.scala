package asciiart.core.filter

import asciiart.core.image.AsciiArt

trait AsciiArtFilter extends Filter[AsciiArt]{
  def apply(image: AsciiArt) : AsciiArt
}
