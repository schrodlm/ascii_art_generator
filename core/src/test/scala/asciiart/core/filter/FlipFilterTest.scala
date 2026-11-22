package asciiart.core.filter

import asciiart.core.image.RGBImage
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class FlipFilterTest extends AnyFlatSpec with Matchers{

  val width = 2
  val height = 3
  val data: Array[Int] = Array(1, 2, 3, 4, 5, 6) // Example pixels, two per row

  val image = new RGBImage(width, height, data)


  "FlipFilter" should "correctly flip an RGB image vertically" in {

    val flipFilter = new FlipFilter(Flip.Vertical)
    val flipped = flipFilter.apply(image)

    // Assertions
    flipped.getWidth() shouldBe width
    flipped.getHeight() shouldBe height
    flipped.getData() shouldBe Array(5, 6, 3, 4, 1, 2)
  }

  it should "correctly flip an RGB image horizontally" in {
    val flipFilter = new FlipFilter(Flip.Horizontal)
    val flipped = flipFilter.apply(image)

    flipped.getData() shouldBe Array(2, 1, 4, 3, 6, 5)
  }

  it should "only accept a direction from Flip" in {
    """new FlipFilter("x")""" shouldNot typeCheck
  }
}
