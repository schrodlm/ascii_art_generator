package asciiart.core.filter

import asciiart.core.image.RGBImage
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ResizeFilterTest extends AnyFlatSpec with Matchers {

  private def px(r: Int, g: Int, b: Int): Int = 0xFF000000 | (r << 16) | (g << 8) | b

  "ResizeFilter" should "leave the image unchanged at its own width with square cells" in {
    val image = new RGBImage(3, 2, Array(px(1, 2, 3), px(4, 5, 6), px(7, 8, 9), px(10, 11, 12), px(13, 14, 15), px(16, 17, 18)))

    val resized = new ResizeFilter(3, 1).apply(image)

    resized.getWidth() shouldBe 3
    resized.getHeight() shouldBe 2
    resized.getData() shouldBe image.getData()
  }

  it should "average the pixels each output pixel covers" in {
    val image = new RGBImage(4, 2, Array(
      px(0, 100, 0), px(10, 100, 1), px(40, 0, 255), px(50, 0, 255),
      px(20, 100, 2), px(30, 100, 3), px(60, 0, 255), px(70, 0, 255)
    ))

    val resized = new ResizeFilter(2, 1).apply(image)

    resized.getWidth() shouldBe 2
    resized.getHeight() shouldBe 1
    resized.getData() shouldBe Array(px(15, 100, 1), px(55, 0, 255))
  }

  it should "use fewer rows for character cells narrower than they are tall" in {
    val image = new RGBImage(4, 4, Array.fill(16)(px(0, 0, 0)))

    val resized = new ResizeFilter(4, 0.5).apply(image)

    resized.getWidth() shouldBe 4
    resized.getHeight() shouldBe 2
  }

  it should "keep at least one row" in {
    val image = new RGBImage(100, 1, Array.fill(100)(px(0, 0, 0)))

    new ResizeFilter(10, 0.5).apply(image).getHeight() shouldBe 1
  }

  it should "throw IllegalArgumentException for an empty image" in {
    for ((width, height) <- Seq((0, 0), (4, 0), (0, 4))) {
      val error = the[IllegalArgumentException] thrownBy new ResizeFilter(5, 0.5).apply(new RGBImage(width, height, Array.empty[Int]))

      error.getMessage shouldBe "The image is empty"
    }
  }

  it should "throw IllegalArgumentException for fewer than one column or a cell aspect that is not finite and positive" in {
    val image = new RGBImage(1, 1, Array(px(0, 0, 0)))

    an[IllegalArgumentException] should be thrownBy new ResizeFilter(0, 0.5).apply(image)
    for (cellAspect <- Seq(0.0, -1.0, Double.NaN, Double.PositiveInfinity)) {
      an[IllegalArgumentException] should be thrownBy new ResizeFilter(4, cellAspect).apply(image)
    }
  }

  it should "give each character several pixels when its cell asks for them" in {
    val resized = new ResizeFilter(4, 0.5, cellWidth = 2, cellHeight = 4).apply(new RGBImage(8, 8, Array.fill(64)(px(0, 0, 0))))

    // A square picture in cells half as wide as tall: 4 columns make 2 rows, each character 2 by 4 pixels.
    (resized.getWidth(), resized.getHeight()) shouldBe ((4 * 2, 2 * 4))
  }

  it should "refuse output too large for an array, without the check itself overflowing" in {
    val image = new RGBImage(4, 4, Array.fill(16)(px(0, 0, 0)))

    for (cellAspect <- Seq(1e30, 2.305843009213694e18)) {
      val error = the[IllegalArgumentException] thrownBy new ResizeFilter(1, cellAspect, 2, 4).apply(image)
      error.getMessage shouldBe "Output would be too large"
    }
  }
}
