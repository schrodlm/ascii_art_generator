package asciiart.core.table

/**
 * Represents a character table used in ASCII art generation.
 *
 * This class holds an array of characters that are used to convert image pixels
 * into ASCII characters based on their intensity.
 *
 * @param chars An array of characters used for ASCII art conversion.
 */
abstract class Table(){
  def getChar(value : Int) : Char
}

// The table uses `chars` directly, without a copy, so don't change it afterwards.
class LinearTable(val chars: Array[Char]) extends Table{
  if (chars.isEmpty) {
    throw new IllegalArgumentException("A character table needs at least one character")
  }

  // Splits the 256 grey values into equal runs, one per character.
  override def getChar(value : Int): Char = chars(value * chars.length / 256)
}

// The table uses `chars` directly, without a copy, so don't change it afterwards.
class NonLinearTable(val chars: Array[(Int,Char)]) extends Table {
  if (chars.isEmpty) {
    throw new IllegalArgumentException("A character table needs at least one character")
  }
  if (chars.exists { case (limit, _) => limit < 0 || limit > 255 }) {
    throw new IllegalArgumentException("Grey limits must be between 0 and 255")
  }
  if (chars.zip(chars.tail).exists { case ((limit, _), (next, _)) => next <= limit }) {
    throw new IllegalArgumentException("Grey limits must rise from one character to the next")
  }
  if (chars.last._1 != 255) {
    throw new IllegalArgumentException("The last grey limit must be 255, so every grey has a character")
  }

  override def getChar(value: Int): Char = {
    chars.find { case (border, _) =>

      value <= border
    }.get._2
  }
}

/**
 * The `DefaultTable` class extends the `Table` class with a predefined set of characters.
 * These characters are typically used for creating simple ASCII art representations,
 * where each character represents a different shade of gray, from darkest to lightest.
 */
class DefaultTable() extends LinearTable(Array('@','%','#','*','+','=','-',':','.', ' '))

class DefaultNonLinearTable() extends NonLinearTable(Array((10,'@'),(40,'%'),(100,'#'),(101,'*'),(130,'+'),(200,'='),(255,'-')))

/**
 * The `PaulBourkeTable` class extends the `Table` class with a specific set of characters
 * designed by Paul Bourke. This set of characters is arranged to represent different
 * levels of intensity, from the lightest to the darkest, allowing for a more detailed
 * and nuanced ASCII art representation. This character set is often used in ASCII art
 * rendering algorithms for its ability to closely mimic grayscale images.
 */
class PaulBourkeTable() extends LinearTable(Array(
  '$', '@', 'B', '%', '8', '&', 'W', 'M', '#', '*', 'o', 'a', 'h', 'k', 'b', 'd', 'p', 'q',
  'w', 'm', 'Z', 'O', '0', 'Q', 'L', 'C', 'J', 'U', 'Y', 'X', 'z', 'c', 'v', 'u', 'n', 'x',
  'r', 'j', 'f', 't', '/', '\\', '|', '(', ')', '1', '{', '}', '[', ']', '?', '-', '_', '+',
  '~', '<', '>', 'i', '!', 'l', 'I', ';', ':', ',', '"', '^', '`', '\'', '.'))

/**
 * A custom character table for ASCII art generation.
 *
 * This table allows for user-defined characters, providing flexibility in the style of the generated ASCII art.
 *
 * @param chars An array of custom characters provided by the user.
 */
class CustomTable(chars: Array[Char]) extends LinearTable(chars)