package clascii.core.conversion

import clascii.core.characters.Dither
import clascii.core.image.GrayscaleImage
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class BrailleConverterTest extends AnyFlatSpec with Matchers {

  private def image(width: Int, height: Int)(grey: (Int, Int) => Int) =
    new GrayscaleImage(width, height, Array.tabulate(width * height)(i => grey(i % width, i / width)))

  private def raisedDots(characters: Array[Int]) = characters.map(c => Integer.bitCount(c - 0x2800)).sum

  "BrailleConverter" should "raise every dot of a black cell and none of a white one" in {
    for (dither <- Seq(Dither.Threshold, Dither.Ordered, Dither.Diffusion)) {
      new BrailleConverter(dither).convert(image(2, 4)((_, _) => 0)).getData() shouldBe Array(0x28FF)
      new BrailleConverter(dither).convert(image(2, 4)((_, _) => 255)).getData() shouldBe Array(0x2800)
    }
  }

  it should "use Unicode's numbering of the dots" in {
    // Only the bottom right pixel is dark: dot 8, bit 0x80.
    val art = new BrailleConverter(Dither.Threshold).convert(image(2, 4)((x, y) => if (x == 1 && y == 3) 0 else 255))

    art.getData() shouldBe Array(0x2880)
  }

  it should "make one character of every 2 by 4 pixels" in {
    val art = new BrailleConverter().convert(image(6, 8)((_, _) => 0))

    (art.getWidth(), art.getHeight()) shouldBe ((3, 2))
  }

  it should "raise about half the dots of mid-grey, unless it only thresholds" in {
    val grey = image(16, 16)((_, _) => 128)

    raisedDots(new BrailleConverter(Dither.Threshold).convert(grey).getData()) shouldBe 0
    raisedDots(new BrailleConverter(Dither.Ordered).convert(grey).getData()) shouldBe 128
    raisedDots(new BrailleConverter(Dither.Diffusion).convert(grey).getData()) shouldBe (128 +- 8)
  }

  it should "reject an image that is not a whole number of cells" in {
    val error = the[IllegalArgumentException] thrownBy new BrailleConverter().convert(image(3, 4)((_, _) => 0))

    error.getMessage shouldBe "A braille image must be a whole number of 2 by 4 cells"
  }

  "Dither" should "be found by name, in any case, ordered first" in {
    Dither.names shouldBe Seq("ordered", "diffusion", "threshold")
    Dither.named("Diffusion") shouldBe Some(Dither.Diffusion)
    Dither.named("floyd") shouldBe None
  }
}
