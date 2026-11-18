package asciiart.cli.parser

import asciiart.core.table.CustomTable
import asciiart.cli.config.{ConsoleImageOutput, PathImageOutput, PathImageSource, RandomImageSource}
import asciiart.core.filter.{BrightnessFilter, ContrastFilter, FlipFilter, InvertFilter, ScaleFilter}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class CommandLineParserTest extends AnyFlatSpec with Matchers {

  "CommandLineParser" should "correctly parse image source and output options" in {

    val args = Array("--image", "/path/to/image.jpg", "--output-console")
    val parser = new CommandLineParser(args)
    val configOption = parser.parse()

    configOption.isDefined shouldBe true
    val config = configOption.get
    config.imageSource shouldBe a[PathImageSource]
    config.imageOutput shouldBe a[ConsoleImageOutput]
  }

  it should "handle custom table option" in {
    val args = Array("--image-random", "--output-file", "test.txt", "--custom-table", ".*+")
    val parser = new CommandLineParser(args)
    val configOption = parser.parse()

    configOption.isDefined shouldBe true
    val config = configOption.get
    config.imageSource shouldBe a[RandomImageSource]
    config.imageOutput shouldBe a[PathImageOutput]
    config.render.table shouldBe a[CustomTable]

    config.render.table.getChar(0) shouldEqual '.'
    config.render.table.getChar(128) shouldEqual '*'
    config.render.table.getChar(255) shouldEqual '+'
  }

  it should "parse console output option" in {
    val args = Array("--image-random", "--output-console")
    val parser = new CommandLineParser(args)
    val config = parser.parse().get

    config.imageOutput shouldBe a[ConsoleImageOutput]
    config.imageSource shouldBe a[RandomImageSource]
  }


  it should "parse scale filter option" in {
    val args = Array("--scale", "2", "--image-random", "--output-console")
    val parser = new CommandLineParser(args)
    val config = parser.parse().get


    config.render.asciiFilters(0) shouldBe (a[ScaleFilter])
  }

  it should "throw when scale filter option is not a double" in {

    val args = Array("--scale", "xyz")

    an[IllegalArgumentException] should be thrownBy {
      new CommandLineParser(args).parse()
    }
  }

  it should "parse brightness filter option" in {
    val args = Array("--brightness", "5", "--image-random", "--output-console")
    val parser = new CommandLineParser(args)
    val config = parser.parse().get

    config.render.grayscaleFilters(0) shouldBe a[BrightnessFilter]
  }

  it should "parse contrast filter option" in {
    val args = Array("--contrast", "1.5", "--image-random", "--output-console")
    val config = new CommandLineParser(args).parse().get

    config.render.grayscaleFilters should have size 1
    config.render.grayscaleFilters(0) shouldBe a[ContrastFilter]
  }

  it should "throw when contrast filter option is not a number" in {
    an[IllegalArgumentException] should be thrownBy new CommandLineParser(Array("--contrast", "xyz")).parse()
  }

  it should "parse invert filter option" in {
    val args = Array("--invert", "--image-random", "--output-console")
    val config = new CommandLineParser(args).parse().get

    config.render.grayscaleFilters should have size 1
    config.render.grayscaleFilters(0) shouldBe an[InvertFilter]
  }

  it should "throw when brightness filter option is not a valid integer" in {

    val args = Array("--brightness", "xyz")

    an[IllegalArgumentException] should be thrownBy {
      new CommandLineParser(args).parse()
    }
  }

  it should "parse flip filter option" in {
    val args = Array("--flip", "horizontal", "--image-random", "--output-console")
    val parser = new CommandLineParser(args)
    val config = parser.parse().get

    config.render.asciiFilters(0) shouldBe (a[FlipFilter])
  }

  // Additional tests for other options like --custom-table, --rotate, --invert, etc.

  it should "throw when provided with invalid argument" in {
    val args = Array("--invalid-option")
    val parser = new CommandLineParser(args)

    an[IllegalArgumentException] shouldBe thrownBy {
      parser.parse()
    }
  }

}

