package asciiart.core.conversion

import asciiart.core.table.Table
import asciiart.core.image.GrayscaleImage
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class GrayscaleToAsciiConverterTest extends AnyFlatSpec with Matchers {
  "GrayscaleToAsciiConverter" should "correctly convert a GrayscaleImage to AsciiArt" in {
    val width = 10
    val height = 10
    val grayscaleData = Array.fill(width * height)(128) // Example grayscale data
    val grayscaleImage = new GrayscaleImage(width, height, grayscaleData)

    val mockTable = new Table {
      override def getChar(value: Int): Char = {
        // Simplified character mapping for testing
        (value / 25 + 65).toChar // Maps grayscale values to ASCII characters
      }
    }

    val converter = new GrayscaleToAsciiConverter(mockTable)
    val asciiArt = converter.convert(grayscaleImage)

    // Assertions
    asciiArt.getWidth() shouldBe width
    asciiArt.getHeight() shouldBe height

    // Check ASCII values
    asciiArt.getData().foreach { asciiValue =>
      asciiValue shouldBe 'F'.toInt // Based on the mock mapping
    }
  }
}
