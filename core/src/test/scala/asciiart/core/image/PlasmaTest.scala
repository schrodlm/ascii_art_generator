package asciiart.core.image

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class PlasmaTest extends AnyFlatSpec with Matchers {

  "Plasma" should "draw a 640 by 480 opaque grey image" in {
    val image = Plasma.generate(7)

    (image.getWidth(), image.getHeight()) shouldBe ((640, 480))
    all(image.getData().map(image.getPixelColor)) should matchPattern {
      case (0xFF, r, g, b) if r == g && g == b =>
    }
  }

  it should "draw the same picture for the same seed and a different one for another seed" in {
    Plasma.generate(7).getData() shouldBe Plasma.generate(7).getData()
    Plasma.generate(7).getData() should not be Plasma.generate(8).getData()
  }

  it should "use most of the brightness range" in {
    val values = Plasma.generate(7).getData().map(_ & 0xFF)

    values.min should be < 64
    values.max should be > 192
  }
}
