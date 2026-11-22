package clascii.core.image

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class AsciiArtTest extends AnyFlatSpec with Matchers {

  private val red = 0xFFFF0000
  private val green = 0xFF00FF00
  private val blue = 0xFF0000FF

  "AsciiArt" should "copy each character together with its colour when rearranged" in {
    val art = new AsciiArt(3, 1, Array('a', 'b', 'c'), Some(Array(red, green, blue)))

    val reversed = art.rearrange(3, 1)((x, y) => (2 - x, y))

    reversed.getData().map(_.toChar) shouldBe Array('c', 'b', 'a')
    reversed.colors.get shouldBe Array(blue, green, red)
  }

  it should "stay without colours when rearranged without any" in {
    new AsciiArt(2, 1, Array('a', 'b')).rearrange(1, 1)((_, _) => (1, 0)).colors shouldBe None
  }

  it should "reject a rearrangement that reaches outside it" in {
    val error = the[IllegalArgumentException] thrownBy new AsciiArt(2, 1, Array('a', 'b')).rearrange(1, 1)((_, _) => (2, 0))

    error.getMessage shouldBe "Cell (2, 0) is outside the 2 by 1 art"
  }

  it should "reject colours that do not match its characters one to one" in {
    val error = the[IllegalArgumentException] thrownBy new AsciiArt(2, 1, Array('a', 'b'), Some(Array(red)))

    error.getMessage shouldBe "There must be one colour for every character"
  }
}
