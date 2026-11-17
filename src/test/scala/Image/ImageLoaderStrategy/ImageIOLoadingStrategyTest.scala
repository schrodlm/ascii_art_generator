package Image.ImageLoaderStrategy

import Image.RGBImage
import org.scalatest.{FlatSpec, Matchers}

import java.awt.image.BufferedImage

class ImageIOLoadingStrategyTest extends FlatSpec with Matchers {
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
