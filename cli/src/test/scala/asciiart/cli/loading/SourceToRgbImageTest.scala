package asciiart.cli.loading

import asciiart.cli.config.PathImageSource
import asciiart.core.image.RGBImage
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SourceToRgbImageTest extends AnyFlatSpec with Matchers {

  "SourceToRgbImage" should "load the image through the injected loading strategy" in {
    val image = new RGBImage(1, 1, Array(0))
    val strategy = new ImageLoadingStrategy {
      override def load(): RGBImage = image
    }
    val converter = new SourceToRgbImage(PathImageSource("test.png"), Some(strategy))

    converter.convert() shouldBe image
  }

  it should "throw IllegalArgumentException for unsupported file formats" in {
    val converter = new SourceToRgbImage(PathImageSource("unsupported.xyz"))
    an[IllegalArgumentException] should be thrownBy converter.convert()
  }
}
