package clascii.cli

import clascii.core.image.AsciiArt
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ColoredTextTest extends AnyFlatSpec with Matchers {

  private val red = 0xFFFF0000
  private val blue = 0xFF0000FF
  private def code(red: Int, green: Int, blue: Int) = s"\u001b[38;2;$red;$green;${blue}m"
  private val reset = "\u001b[0m"

  "ColoredText" should "be the plain text when the art has no colours" in {
    ColoredText.of(new AsciiArt(2, 2, Array('a', 'b', 'c', 'd'))) shouldBe "ab\ncd"
  }

  it should "colour each character, write a colour only when it changes, and reset every line" in {
    val art = new AsciiArt(3, 2, Array('a', 'b', 'c', 'd', 'e', 'f'), Some(Array(red, red, blue, blue, blue, blue)))

    ColoredText.of(art) shouldBe
      s"${code(255, 0, 0)}ab${code(0, 0, 255)}c$reset\n${code(0, 0, 255)}def$reset"
  }

  it should "tell white apart from no colour yet" in {
    val white = 0xFFFFFFFF // -1 as an Int

    ColoredText.of(new AsciiArt(1, 1, Array('w'), Some(Array(white)))) shouldBe s"${code(255, 255, 255)}w$reset"
  }
}
