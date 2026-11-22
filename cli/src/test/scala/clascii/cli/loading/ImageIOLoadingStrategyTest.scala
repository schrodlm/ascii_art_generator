package clascii.cli.loading

import clascii.core.image.RGBImage
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.awt.image.BufferedImage

class ImageIOLoadingStrategyTest extends AnyFlatSpec with Matchers {
  "ImageIOLoadingStrategy" should "correctly load an image and apply the converter" in {
    val path = "test.jpg"
    val bufferedImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)
    val rgbImage = new RGBImage(1, 1, Array(0))
    var converted: Option[BufferedImage] = None
    val converter: BufferedImage => RGBImage = { image =>
      converted = Some(image)
      rgbImage
    }

    val strategy = new ImageIOLoadingStrategy(path, converter) {
      override protected def readImage(path: String): BufferedImage = bufferedImage
    }

    strategy.load() shouldBe rgbImage
    converted shouldBe Some(bufferedImage)
  }
}
