package clascii.core.filter

import clascii.core.image.AsciiArt

trait AsciiArtFilter extends Filter[AsciiArt]{
  def apply(image: AsciiArt) : AsciiArt
}
