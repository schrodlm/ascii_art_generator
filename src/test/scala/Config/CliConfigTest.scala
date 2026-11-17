package Config

import Config.Tables.{CustomTable, DefaultTable, PaulBorkesTable}
import Filter.{AsciiArtFilter, GrayscaleImageFilter, RGBImageFilter}
import Image.{AsciiArt, GrayscaleImage, RGBImage}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class CliConfigTest extends AnyFlatSpec with Matchers {
  private def newAsciiFilter(): AsciiArtFilter = new AsciiArtFilter {
    def apply(image: AsciiArt): AsciiArt = image
  }
  private def newGrayscaleFilter(): GrayscaleImageFilter = new GrayscaleImageFilter {
    def apply(image: GrayscaleImage): GrayscaleImage = image
  }
  private def newRgbFilter(): RGBImageFilter = new RGBImageFilter {
    def apply(image: RGBImage): RGBImage = image
  }


  "CliConfig builder" should "correctly build a config with all properties set" in {
    val source = PathImageSource("x.png")
    val output = ConsoleImageOutput()
    val chars = Array('.', ':', '-', '=', '+', '*')
    val asciiFilter = newAsciiFilter()
    val grayscaleFilter = newGrayscaleFilter()
    val rgbFilter = newRgbFilter()

    val config = new CliConfig.Builder()
      .withImageSource(source)
      .withImageOutput(output)
      .withTable(chars)
      .addFilter(asciiFilter)
      .addFilter(grayscaleFilter)
      .addFilter(rgbFilter)
      .build()

    config.imageSource shouldBe source
    config.imageOutput shouldBe output
    config.render.table shouldBe a[CustomTable]
    config.render.asciiFilters should contain theSameElementsAs Array(asciiFilter)
    config.render.grayscaleFilters should contain theSameElementsAs Array(grayscaleFilter)
    config.render.rgbFilters should contain theSameElementsAs Array(rgbFilter)

  }

  it should "build when name of available tables are provided" in {
    val source = PathImageSource("x.png")
    val output = ConsoleImageOutput()
    val PaulBorkeTable = "PaulBorkes"


    val config = new CliConfig.Builder()
      .withImageSource(source)
      .withImageOutput(output)
      .withTable(PaulBorkeTable)
      .build()

    config.render.table shouldBe a[PaulBorkesTable]

  }

  it should "build when no custom table is provided (by using default table)" in {
    val source = PathImageSource("x.png")
    val output = ConsoleImageOutput()

    val config = new CliConfig.Builder()
      .withImageSource(source)
      .withImageOutput(output)
      .build()

    config.imageSource shouldBe source
    config.imageOutput shouldBe output
    config.render.table shouldBe a[DefaultTable]
  }


  it should "throw an exception when image source is not set" in {

    val output = ConsoleImageOutput()

    an[IllegalStateException] should be thrownBy {
      new CliConfig.Builder()
        .withImageOutput(output)
        .build()
    }
  }

  it should "throw an exception when image output is not set" in {

    val source = PathImageSource("x.png")

    an[IllegalStateException] should be thrownBy {
      new CliConfig.Builder()
        .withImageSource(source)
        .build()
    }
  }

  it should "build correctly with only image source and image output " in {

    val source = PathImageSource("x.png")
    val output = ConsoleImageOutput()

     val config =  new CliConfig.Builder()
        .withImageSource(source)
        .withImageOutput(output)
        .build()

    config.imageSource shouldBe source
    config.imageOutput shouldBe output
    config.render.table shouldBe a[DefaultTable]
    config.render.asciiFilters shouldBe empty
    config.render.grayscaleFilters shouldBe empty
    config.render.rgbFilters shouldBe empty

  }

  it should "correctly categorize filters" in {
    val asciiFilter = newAsciiFilter()
    val grayscaleFilter = newGrayscaleFilter()
    val rgbFilter = newRgbFilter()

    val config = new CliConfig.Builder()
      .withImageSource(PathImageSource("x.png"))
      .withImageOutput(ConsoleImageOutput())
      .addFilter(asciiFilter)
      .addFilter(grayscaleFilter)
      .addFilter(rgbFilter)
      .build()

    config.render.asciiFilters should contain only asciiFilter
    config.render.grayscaleFilters should contain only grayscaleFilter
    config.render.rgbFilters should contain only rgbFilter
  }

  it should "throw an exception when provided with two image sources" in {

    val source1 = PathImageSource("x.png")
    val source2 = PathImageSource("x.png")

    an[IllegalArgumentException] should be thrownBy {
      new CliConfig.Builder()
        .withImageSource(source1)
        .withImageSource(source2)
        .build()
    }
  }

}
