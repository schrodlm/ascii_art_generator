package asciiart.core.pipeline

import asciiart.core.filter.{Flip, FlipFilter, InvertFilter, RotationFilter}
import asciiart.core.image.RGBImage
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class AsciiArtPipelineTest extends AnyFlatSpec with Matchers {

  private val black = 0xFF000000
  private val red = 0xFFFF0000
  private val green = 0xFF00FF00
  private val blue = 0xFF0000FF
  private val white = 0xFFFFFFFF

  private def blank(width: Int, height: Int) = new RGBImage(width, height, Array.fill(width * height)(0xFF000000))

  "The pipeline" should "rotate before resizing, so the cell shape applies to the rotated picture" in {
    val settings = RenderSettings(rgbFilters = Seq(new RotationFilter(90)))

    val art = AsciiArtPipeline.render(blank(4, 2), settings)

    // Rotated to 2x4, then 4 rows at half height. Resizing first would give 1x4.
    (art.getWidth(), art.getHeight()) shouldBe ((2, 2))
  }

  it should "fit a wider image into at most the given columns without enlarging a narrower one" in {
    val wide = AsciiArtPipeline.render(blank(16, 8), RenderSettings(maxColumns = 8))
    val narrow = AsciiArtPipeline.render(blank(16, 8), RenderSettings(maxColumns = 100))

    (wide.getWidth(), wide.getHeight()) shouldBe ((8, 2))
    (narrow.getWidth(), narrow.getHeight()) shouldBe ((16, 4))
  }

  it should "fit inside a box of columns and rows, whichever limit is tighter" in {
    val box = RenderSettings(maxColumns = 120, maxRows = 30)

    val landscape = AsciiArtPipeline.render(blank(320, 240), box)
    val portrait = AsciiArtPipeline.render(blank(240, 320), box)
    val roomy = AsciiArtPipeline.render(blank(320, 240), RenderSettings(maxColumns = 120, maxRows = 100))

    (landscape.getWidth(), landscape.getHeight()) shouldBe ((80, 30))
    (portrait.getWidth(), portrait.getHeight()) shouldBe ((45, 30))
    (roomy.getWidth(), roomy.getHeight()) shouldBe ((120, 45))
  }

  it should "keep each character's colour only when asked to" in {
    // cellAspect = 1 keeps cells square, so each pixel becomes one character.
    val image = new RGBImage(4, 1, Array(red, green, blue, white))

    AsciiArtPipeline.render(image, RenderSettings(cellAspect = 1)).colors shouldBe None
    AsciiArtPipeline.render(image, RenderSettings(cellAspect = 1, colors = true)).colors.get shouldBe image.getData()
  }

  it should "average the colours each character covers" in {
    // Two pixels side by side fill one cell twice as wide as tall: mid-grey, rounded down.
    val image = new RGBImage(2, 1, Array(black, white))
    val settings = RenderSettings(maxColumns = 1, cellAspect = 2, colors = true)

    AsciiArtPipeline.render(image, settings).colors.get shouldBe Array(0xFF7F7F7F)
  }

  it should "keep colours through grayscale filters and move them with flips" in {
    val image = new RGBImage(2, 1, Array(red, blue))
    val settings = RenderSettings(
      cellAspect = 1,
      colors = true,
      grayscaleFilters = Seq(new InvertFilter()),
      asciiFilters = Seq(new FlipFilter(Flip.Horizontal))
    )

    AsciiArtPipeline.render(image, settings).colors.get shouldBe Array(blue, red)
  }
}
