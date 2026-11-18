package asciiart.core.filter

import asciiart.core.image.AsciiArt
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ScaleFilterTest extends AnyFlatSpec with Matchers {


  val width = 4
  val height = 4
  val data: Array[Int] = Array(
    'a', 'b', 'c', 'd',
    'e', 'f', 'g', 'h',
    'i', 'j', 'k', 'l',
    'm', 'n', 'o', 'p'
  ).map(c => c.toInt) // Example data

  val asciiArt = new AsciiArt(width, height, data)

  "ScaleFilter" should "double the size for a factor of 2" in {
    val scaledArt = new ScaleFilter(2).apply(asciiArt)

    scaledArt.getWidth() shouldBe 8
    scaledArt.getHeight() shouldBe 8
    scaledArt.getData() shouldBe Array(
      'a', 'a', 'b', 'b', 'c', 'c', 'd', 'd',
      'a', 'a', 'b', 'b', 'c', 'c', 'd', 'd',
      'e', 'e', 'f', 'f', 'g', 'g', 'h', 'h',
      'e', 'e', 'f', 'f', 'g', 'g', 'h', 'h',
      'i', 'i', 'j', 'j', 'k', 'k', 'l', 'l',
      'i', 'i', 'j', 'j', 'k', 'k', 'l', 'l',
      'm', 'm', 'n', 'n', 'o', 'o', 'p', 'p',
      'm', 'm', 'n', 'n', 'o', 'o', 'p', 'p'
    ).map(c => c.toInt)
  }

  it should "halve the size for a factor of 0.5" in {
    val scaledArt = new ScaleFilter(0.5).apply(asciiArt)

    scaledArt.getWidth() shouldBe 2
    scaledArt.getHeight() shouldBe 2
    scaledArt.getData() shouldBe Array('f', 'h', 'n', 'p')
  }

  it should "throw IllegalArgumentException for non-positive scale values" in {
    val scaleFilter = new ScaleFilter(0)

    an[IllegalArgumentException] should be thrownBy scaleFilter.apply(asciiArt)
  }
}
