package clascii.core.filter

import clascii.core.image.GrayscaleImage
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class InvertFilterTest extends AnyFlatSpec with Matchers {

  "InvertFilter" should "map every brightness value v to 255 - v" in {
    val image = new GrayscaleImage(3, 1, Array(0, 100, 255))

    val inverted = new InvertFilter().apply(image)

    inverted.getWidth() shouldBe 3
    inverted.getHeight() shouldBe 1
    inverted.getData() shouldBe Array(255, 155, 0)
  }
}
