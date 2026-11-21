package asciiart.core.filter

import asciiart.core.image.GrayscaleImage
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class BrightnessFilterTest extends AnyFlatSpec with Matchers{

  "BrightnessFilter" should "correctly adjust the brightness of a GrayscaleImage" in {
    val width = 10
    val height = 10
    val brightnessAdjustment = 20
    val originalGrayscaleData = Array.fill(width * height)(100) // Example grayscale data
    val grayscaleImage = new GrayscaleImage(width, height, originalGrayscaleData)

    val brightnessFilter = new BrightnessFilter(brightnessAdjustment)
    val adjustedImage = brightnessFilter.apply(grayscaleImage)

    // Assertions
    adjustedImage.getWidth() shouldBe width
    adjustedImage.getHeight() shouldBe height

    // Check adjusted grayscale values
    adjustedImage.getData().foreach { adjustedPixel =>
      val expectedAdjustedValue = (100 + brightnessAdjustment) max 0 min 255
      adjustedPixel shouldBe expectedAdjustedValue
    }
  }

  it should "clamp brightness adjustments to the 0-255 range" in {
    val originalGrayscaleData = Array(10, 245) // Edge values for testing clamping
    val grayscaleImage = new GrayscaleImage(1, 2, originalGrayscaleData)
    val highBrightnessFilter = new BrightnessFilter(100)
    val lowBrightnessFilter = new BrightnessFilter(-100)

    val highAdjustedImage = highBrightnessFilter.apply(grayscaleImage)
    val lowAdjustedImage = lowBrightnessFilter.apply(grayscaleImage)

    highAdjustedImage.getData()(0) shouldBe 110
    highAdjustedImage.getData()(1) shouldBe 255 // Clamped to 255

    lowAdjustedImage.getData()(0) shouldBe 0   // Clamped to 0
    lowAdjustedImage.getData()(1) shouldBe 145
  }

  it should "not overflow with the largest and smallest amounts" in {
    val image = new GrayscaleImage(2, 1, Array(0, 255))

    new BrightnessFilter(Int.MaxValue).apply(image).getData() shouldBe Array(255, 255)
    new BrightnessFilter(Int.MinValue).apply(image).getData() shouldBe Array(0, 0)
  }
}
