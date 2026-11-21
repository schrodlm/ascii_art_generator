package asciiart.cli.config

import asciiart.core.table.{CustomTable, DefaultTable, PaulBourkeTable}
import asciiart.core.filter.{AsciiArtFilter, BrightnessFilter, FlipFilter, GrayscaleImageFilter, RGBImageFilter}
import asciiart.core.image.{AsciiArt, GrayscaleImage, RGBImage}
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
      .addAsciiFilter(asciiFilter)
      .addGrayscaleFilter(grayscaleFilter)
      .addRgbFilter(rgbFilter)
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

    config.render.table shouldBe a[PaulBourkeTable]

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

    val error = the[IllegalArgumentException] thrownBy {
      new CliConfig.Builder()
        .withImageOutput(output)
        .build()
    }
    error.getMessage shouldBe "Missing an image source: use --image [path] or --image-random"
  }

  it should "throw an exception when image output is not set" in {

    val source = PathImageSource("x.png")

    val error = the[IllegalArgumentException] thrownBy {
      new CliConfig.Builder()
        .withImageSource(source)
        .build()
    }
    error.getMessage shouldBe "Missing an output: use --output-console or --output-file [path]"
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

  it should "keep the order of filters within a stage" in {
    val first = newAsciiFilter()
    val second = newAsciiFilter()

    val config = new CliConfig.Builder()
      .withImageSource(PathImageSource("x.png"))
      .withImageOutput(ConsoleImageOutput())
      .addAsciiFilter(first)
      .addAsciiFilter(second)
      .build()

    config.render.asciiFilters shouldBe Seq(first, second)
  }

  it should "not compile when a filter is added to another stage" in {
    "new CliConfig.Builder().addGrayscaleFilter(new BrightnessFilter(1))" should compile
    "new CliConfig.Builder().addAsciiFilter(new FlipFilter(\"x\"))" should compile
    "new CliConfig.Builder().addAsciiFilter(new BrightnessFilter(1))" shouldNot typeCheck
    "new CliConfig.Builder().addGrayscaleFilter(new FlipFilter(\"x\"))" shouldNot typeCheck
  }

  "CliConfig render settings" should "use the requested width over the terminal's" in {
    val config = new CliConfig.Builder()
      .withImageSource(PathImageSource("x.png"))
      .withImageOutput(ConsoleImageOutput())
      .withWidth(80)
      .build()

    config.renderSettings(Some(120)).maxColumns shouldBe 80
  }

  it should "fit the terminal when printing to it" in {
    val config = new CliConfig.Builder()
      .withImageSource(PathImageSource("x.png"))
      .withImageOutput(ConsoleImageOutput())
      .build()

    config.renderSettings(Some(120)).maxColumns shouldBe 120
    config.renderSettings(None).maxColumns shouldBe 100
  }

  it should "use 100 columns for a file without asking the terminal" in {
    val config = new CliConfig.Builder()
      .withImageSource(PathImageSource("x.png"))
      .withImageOutput(PathImageOutput("out.txt"))
      .build()
    var asked = false

    config.renderSettings({ asked = true; Some(120) }).maxColumns shouldBe 100
    asked shouldBe false
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
