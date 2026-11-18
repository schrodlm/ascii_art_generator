package asciiart.core.filter

import asciiart.core.image.RGBImage
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class RotationFilterTest extends AnyFlatSpec with Matchers {

  // 3 wide, 2 tall:  1 2 3
  //                  4 5 6
  val image = new RGBImage(3, 2, Array(1, 2, 3, 4, 5, 6))

  "RotationFilter" should "turn the image clockwise by 90 degrees" in {
    val rotated = new RotationFilter(90).apply(image)

    rotated.getWidth() shouldBe 2
    rotated.getHeight() shouldBe 3
    rotated.getData() shouldBe Array(4, 1, 5, 2, 6, 3)
  }

  it should "turn the image upside down by 180 degrees" in {
    val rotated = new RotationFilter(180).apply(image)

    rotated.getWidth() shouldBe 3
    rotated.getHeight() shouldBe 2
    rotated.getData() shouldBe Array(6, 5, 4, 3, 2, 1)
  }

  it should "turn the image anticlockwise for 270 and -90 degrees" in {
    for (degrees <- Seq(270, -90)) {
      val rotated = new RotationFilter(degrees).apply(image)

      rotated.getWidth() shouldBe 2
      rotated.getHeight() shouldBe 3
      rotated.getData() shouldBe Array(3, 6, 2, 5, 1, 4)
    }
  }

  it should "leave the image unchanged for a full turn" in {
    new RotationFilter(360).apply(image).getData() shouldBe image.getData()
  }

  it should "throw IllegalArgumentException for an angle that is not a multiple of 90" in {
    an[IllegalArgumentException] should be thrownBy new RotationFilter(45).apply(image)
  }
}
