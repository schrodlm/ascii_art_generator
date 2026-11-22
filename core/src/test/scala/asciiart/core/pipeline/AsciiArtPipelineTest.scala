package asciiart.core.pipeline

import asciiart.core.characters.Dither
import asciiart.core.conversion.BrailleConverter
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
      rgbFilters = Seq(new FlipFilter(Flip.Horizontal)),
      grayscaleFilters = Seq(new InvertFilter())
    )

    AsciiArtPipeline.render(image, settings).colors.get shouldBe Array(blue, red)
  }

  it should "draw braille in the same number of characters as a table" in {
    val table = AsciiArtPipeline.render(blank(160, 120), RenderSettings(maxColumns = 40))
    val braille = AsciiArtPipeline.render(blank(160, 120), RenderSettings(maxColumns = 40, characterConverter = new BrailleConverter()))

    (braille.getWidth(), braille.getHeight()) shouldBe ((table.getWidth(), table.getHeight()))
    all(braille.getData().toSeq) shouldBe 0x28FF
  }

  it should "give braille no more columns than the picture has pixel pairs for" in {
    AsciiArtPipeline.render(blank(10, 8), RenderSettings(characterConverter = new BrailleConverter())).getWidth() shouldBe 5
  }

  it should "colour each braille character with the average of its 2 by 4 pixels" in {
    // Left column red, right column blue, in cells half as wide as tall: one character, half of each
    // (255 / 2 rounds down to 0x7F).
    val image = new RGBImage(2, 4, Array.tabulate(8)(i => if (i % 2 == 0) red else blue))
    val settings = RenderSettings(characterConverter = new BrailleConverter(), colors = true)

    AsciiArtPipeline.render(image, settings).colors.get shouldBe Array(0xFF7F007F)
  }

  it should "mirror the dots inside braille characters when flipping" in {
    // One cell whose top left pixel alone is dark: mirrored across it is the top right dot, upside
    // down the bottom left.
    val image = new RGBImage(2, 4, Array.tabulate(8)(i => if (i == 0) black else white))
    def flipped(flip: Flip) = AsciiArtPipeline.render(image, RenderSettings(
      characterConverter = new BrailleConverter(Dither.Threshold),
      rgbFilters = Seq(new FlipFilter(flip))
    )).getData()

    flipped(Flip.Horizontal) shouldBe Array(0x2808)
    flipped(Flip.Vertical) shouldBe Array(0x2840)
  }
}
