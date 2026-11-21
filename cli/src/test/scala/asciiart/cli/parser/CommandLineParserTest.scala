package asciiart.cli.parser

import asciiart.core.table.{CustomTable, TableNameMapper}
import asciiart.cli.config.{ConsoleImageOutput, PathImageOutput, PathImageSource, RandomImageSource}
import asciiart.core.filter.{BrightnessFilter, ContrastFilter, Flip, FlipFilter, InvertFilter, RotationFilter, ScaleFilter}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class CommandLineParserTest extends AnyFlatSpec with Matchers {

  // A complete command line, so parsing fails on the option under test and not
  // on the missing image source or output.
  private def parseError(option: String*): String = {
    val args = (option ++ Seq("--image-random", "--output-console")).toArray
    (the[IllegalArgumentException] thrownBy new CommandLineParser(args).parse()).getMessage
  }

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
    parseError("--scale", "xyz") shouldBe "Scale value must be a valid number"
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
    parseError("--contrast", "xyz") shouldBe "Contrast value must be a valid number"
  }

  it should "parse rotate option into the RGB stage" in {
    val args = Array("--rotate", "90", "--image-random", "--output-console")
    val config = new CommandLineParser(args).parse().get

    config.render.rgbFilters should have size 1
    config.render.rgbFilters(0) shouldBe a[RotationFilter]
  }

  it should "throw when rotate option is not a whole number" in {
    parseError("--rotate", "xyz") shouldBe "Rotation value must be a whole number"
  }

  it should "parse width option as the maximum number of columns" in {
    val args = Array("--width", "80", "--image-random", "--output-console")

    new CommandLineParser(args).parse().get.width shouldBe Some(80)
  }

  it should "throw when width option is not a whole number of at least 1" in {
    for (value <- Seq("abc", "0", "-5")) {
      parseError("--width", value) shouldBe "Width must be a whole number of at least 1"
    }
  }

  it should "parse font aspect ratio option as the cell aspect" in {
    def cellAspect(ratio: String): Double =
      new CommandLineParser(Array("--font-aspect-ratio", ratio, "--image-random", "--output-console")).parse().get.render.cellAspect

    cellAspect("1:2") shouldBe 0.5
    cellAspect("3:5") shouldBe 0.6
    cellAspect("1:1") shouldBe 1.0
    cellAspect("10:1") shouldBe 10.0
    cellAspect("1:10") shouldBe 0.1
    cellAspect("2000000000:1000000000") shouldBe 2.0
  }

  it should "throw when font aspect ratio option is not two positive whole numbers between 1:10 and 10:1" in {
    for (ratio <- Seq("abc", "1", "0:2", "1:0", "-1:2", "1:2:3", "1:2:", "11:1", "1:11", "2147483647:1")) {
      withClue(ratio) {
        parseError("--font-aspect-ratio", ratio) shouldBe
          "Font aspect ratio must be two positive whole numbers between 1:10 and 10:1, like 1:2"
      }
    }
  }

  it should "parse invert filter option" in {
    val args = Array("--invert", "--image-random", "--output-console")
    val config = new CommandLineParser(args).parse().get

    config.render.grayscaleFilters should have size 1
    config.render.grayscaleFilters(0) shouldBe an[InvertFilter]
  }

  it should "throw when brightness filter option is not a valid integer" in {
    parseError("--brightness", "xyz") shouldBe "Brightness value must be a valid number"
  }

  it should "parse flip filter option" in {
    val args = Array("--flip", "horizontal", "--image-random", "--output-console")
    val parser = new CommandLineParser(args)
    val config = parser.parse().get

    config.render.asciiFilters(0) shouldBe (a[FlipFilter])
  }

  it should "turn every --flip axis name into its direction" in {
    val directions = Seq("x" -> Flip.Vertical, "vertical" -> Flip.Vertical, "Y" -> Flip.Horizontal,
      "horizontal" -> Flip.Horizontal)
    for ((axis, direction) <- directions) {
      val config = new CommandLineParser(Array("--flip", axis, "--image-random", "--output-console")).parse().get

      config.render.asciiFilters.collect { case filter: FlipFilter => filter.flip } shouldBe Seq(direction)
    }
  }

  it should "reject an unknown --flip axis while reading the options" in {
    parseError("--flip", "z") shouldBe "Axis provided for the flip filter must be 'x' or 'y'"
  }

  // Additional tests for other options like --custom-table, --rotate, --invert, etc.

  it should "parse seed option into the random image source, before or after it" in {
    for (args <- Seq(Array("--image-random", "--seed", "7"), Array("--seed", "7", "--image-random"))) {
      new CommandLineParser(args :+ "--output-console").parse().get.imageSource shouldBe RandomImageSource(Some(7))
    }
  }

  it should "throw when seed option is not a whole number or there is no random image" in {
    parseError("--seed", "abc") shouldBe "Seed must be a whole number"

    val withFile = new CommandLineParser(Array("--image", "x.png", "--seed", "7", "--output-console"))
    (the[IllegalArgumentException] thrownBy withFile.parse()).getMessage shouldBe "--seed only works with --image-random"
  }

  it should "describe every option it accepts and every built-in table within 80 columns" in {
    val usage = new CommandLineParser(Array.empty).usage
    val options = Seq("--image", "--image-random", "--seed", "--output-console", "--output-file", "--width",
      "--font-aspect-ratio", "--rotate", "--brightness", "--contrast", "--invert", "--table", "--custom-table",
      "--flip", "--scale", "--help")
    val tables = TableNameMapper.names

    for (option <- options) usage should include(s"  $option ")
    // Compared without line breaks, so wrapping the help does not matter.
    usage.split("\\s+").mkString(" ") should include(
      s"A built-in table: ${tables.init.mkString(", ")} or ${tables.last}.")
    usage should not include "NOT IMPLEMENTED"
    all(usage.linesIterator.map(_.length).toSeq) should be <= 80
  }

  it should "throw when provided with invalid argument" in {
    parseError("--invalid-option") shouldBe "Unknown option --invalid-option"
  }

}

