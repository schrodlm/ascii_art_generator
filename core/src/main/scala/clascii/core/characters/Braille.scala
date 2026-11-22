package clascii.core.characters

/**
 * Braille characters (U+2800 to U+28FF): each one a cell of 2 by 4 dots, and Unicode gives every
 * dot one bit, so a character is Blank plus the bits of its raised dots.
 */
private[core] object Braille {

  /** The braille character with no dots raised. */
  val Blank = 0x2800

  /** The bit Unicode gives the dot in column x (0 or 1) and row y (0 to 3) of a cell. */
  val Dot: Array[Array[Int]] = Array(Array(0x01, 0x02, 0x04, 0x40), Array(0x08, 0x10, 0x20, 0x80))
}
