package asciiart.core.filter

import asciiart.core.image.AsciiArt
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class FlipFilterTest extends AnyFlatSpec with Matchers{

  val width = 2
  val height = 3
  val data: Array[Int] = Array('a', 'b', 'c', 'd', 'e', 'f').map(c => c.toInt) // Example data

  val asciiArt = new AsciiArt(width, height, data)


  "FlipFilter" should "correctly flip an AsciiArt image vertically" in {

    val flipFilter = new FlipFilter(Flip.Vertical)
    val flippedArt = flipFilter.apply(asciiArt)

    // Assertions
    flippedArt.getWidth() shouldBe width
    flippedArt.getHeight() shouldBe height
    flippedArt.getData() shouldBe Array('e', 'f', 'c', 'd', 'a', 'b')
  }

  it should "correctly flip an AsciiArt image horizontally" in {
    val flipFilter = new FlipFilter(Flip.Horizontal)
    val flippedArt = flipFilter.apply(asciiArt)

    flippedArt.getData() shouldBe Array('b', 'a', 'd', 'c', 'f', 'e')
  }

  it should "only accept a direction from Flip" in {
    """new FlipFilter("x")""" shouldNot typeCheck
  }

  it should "move each colour with its character" in {
    val colored = new AsciiArt(2, 2, Array('a', 'b', 'c', 'd'), Some(Array(1, 2, 3, 4)))

    new FlipFilter(Flip.Vertical).apply(colored).colors.get shouldBe Array(3, 4, 1, 2)
    new FlipFilter(Flip.Horizontal).apply(colored).colors.get shouldBe Array(2, 1, 4, 3)
  }
}
