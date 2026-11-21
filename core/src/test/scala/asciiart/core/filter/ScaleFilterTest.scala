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

  it should "cover the whole image for a non-integer factor" in {
    val scaledArt = new ScaleFilter(1.5).apply(asciiArt)

    scaledArt.getWidth() shouldBe 6
    scaledArt.getHeight() shouldBe 6
    scaledArt.getData() shouldBe Array(
      'a', 'b', 'b', 'c', 'd', 'd',
      'e', 'f', 'f', 'g', 'h', 'h',
      'e', 'f', 'f', 'g', 'h', 'h',
      'i', 'j', 'j', 'k', 'l', 'l',
      'm', 'n', 'n', 'o', 'p', 'p',
      'm', 'n', 'n', 'o', 'p', 'p'
    ).map(c => c.toInt)
  }

  it should "keep at least one character when the factor shrinks below it" in {
    val scaledArt = new ScaleFilter(0.1).apply(asciiArt)

    scaledArt.getWidth() shouldBe 1
    scaledArt.getHeight() shouldBe 1
    scaledArt.getData() shouldBe Array('k')
  }

  it should "throw IllegalArgumentException for non-positive scale values" in {
    val scaleFilter = new ScaleFilter(0)

    an[IllegalArgumentException] should be thrownBy scaleFilter.apply(asciiArt)
  }

  it should "throw IllegalArgumentException for factors that are not finite" in {
    for (scale <- Seq(Double.NaN, Double.PositiveInfinity)) {
      an[IllegalArgumentException] should be thrownBy new ScaleFilter(scale).apply(asciiArt)
    }
  }

  it should "throw IllegalArgumentException when the output would not fit in an array" in {
    an[IllegalArgumentException] should be thrownBy new ScaleFilter(1e6).apply(asciiArt)
  }

  it should "keep a very wide image unchanged at a factor of 1" in {
    val wide = new AsciiArt(40000, 1, Array.tabulate(40000)(identity))

    new ScaleFilter(1).apply(wide).getData() shouldBe wide.getData()
  }

  it should "copy each colour with its character" in {
    val colored = new AsciiArt(2, 1, Array('a', 'b'), Some(Array(1, 2)))

    val doubled = new ScaleFilter(2).apply(colored)

    doubled.getData().map(_.toChar) shouldBe Array('a', 'a', 'b', 'b', 'a', 'a', 'b', 'b')
    doubled.colors.get shouldBe Array(1, 1, 2, 2, 1, 1, 2, 2)
  }
}
