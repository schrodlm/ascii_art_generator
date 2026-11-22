package clascii.core.filter

import clascii.core.image.GrayscaleImage
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ContrastFilterTest extends AnyFlatSpec with Matchers {

  val image = new GrayscaleImage(4, 1, Array(0, 100, 200, 255))

  "ContrastFilter" should "leave the image unchanged for a factor of 1" in {
    new ContrastFilter(1).apply(image).getData() shouldBe Array(0, 100, 200, 255)
  }

  it should "push values away from mid-grey and clamp them for a factor above 1" in {
    val result = new ContrastFilter(2).apply(image)

    result.getWidth() shouldBe 4
    result.getHeight() shouldBe 1
    result.getData() shouldBe Array(0, 72, 255, 255)
  }

  it should "pull values towards mid-grey for a factor below 1" in {
    new ContrastFilter(0.5).apply(image).getData() shouldBe Array(64, 114, 164, 192)
  }

  it should "clamp instead of overflowing for a huge factor" in {
    new ContrastFilter(1e20).apply(image).getData() shouldBe Array(0, 0, 255, 255)
  }

  it should "make every value mid-grey for a factor of 0" in {
    new ContrastFilter(0).apply(image).getData() shouldBe Array(128, 128, 128, 128)
  }

  it should "throw IllegalArgumentException for a negative or non-finite factor" in {
    for (factor <- Seq(-1.0, Double.NaN, Double.PositiveInfinity)) {
      an[IllegalArgumentException] should be thrownBy new ContrastFilter(factor).apply(image)
    }
  }
}
