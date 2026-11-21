package asciiart.cli

import asciiart.core.image.AsciiArt

/**
 * The art as text for a terminal: when the art has colours, each character is preceded by a
 * 24-bit colour code (ESC[38;2;red;green;bluem), written only when the colour changes, and every
 * line ends with a reset so the colour does not spill into the rest of the terminal.
 */
object ColoredText {

  def of(art: AsciiArt): String = art.colors match {
    case None => art.toText
    case Some(colors) =>
      val width = art.getWidth()
      val characters = art.getData()
      val text = new StringBuilder
      for (row <- 0 until art.getHeight()) {
        if (row > 0) text += '\n'
        var previous = -1 // no colour yet; a 24-bit colour is never -1
        for (i <- row * width until (row + 1) * width) {
          val rgb = colors(i) & 0xFFFFFF
          if (rgb != previous) {
            text ++= s"\u001b[38;2;${rgb >> 16};${(rgb >> 8) & 0xFF};${rgb & 0xFF}m"
            previous = rgb
          }
          text += characters(i).toChar
        }
        text ++= "\u001b[0m"
      }
      text.toString
  }
}
