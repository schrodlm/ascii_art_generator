package asciiart.core.pipeline

import asciiart.core.conversion.TableConverter
import asciiart.core.filter.BrightnessFilter
import asciiart.core.image.{Plasma, RGBImage}
import asciiart.core.table.PaulBourkeTable
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 * Pins the exact text the pipeline produces for a fixed image. This suite runs
 * on the JVM and on JS; both passing proves the two platforms render the same
 * image and settings to the same text.
 */
class GoldenRenderTest extends AnyFlatSpec with Matchers {

  // 16x8 colour gradient: red grows to the right, green downwards, blue along
  // the diagonal, so the grayscale weights are all exercised.
  private val fixture: RGBImage = {
    val w = 16
    val h = 8
    val pixels = Array.tabulate(w * h) { i =>
      val x = i % w
      val y = i / w
      (0xFF << 24) | ((x * 17) << 16) | ((y * 36) << 8) | ((x + y) * 11)
    }
    new RGBImage(w, h, pixels)
  }

  "The pipeline" should "render the fixture with the default settings" in {
    AsciiArtPipeline.render(fixture, RenderSettings()).toText shouldBe
      """@@@%%%%####****+
        |####****++++====
        |*++++====----:::
        |==----::::....  """.stripMargin
  }

  it should "render the fixture with square cells" in {
    val art = AsciiArtPipeline.render(fixture, RenderSettings(cellAspect = 1))

    art.toText shouldBe
      """@@@@@%%%%####***
        |@%%%%####****+++
        |%%####****++++==
        |##****++++====--
        |***++++====----:
        |+++====----::::.
        |====----::::....
        |----::::....    """.stripMargin
  }

  it should "render the fixture with the Paul Bourke table, square cells and a brightness filter" in {
    val settings = RenderSettings(
      characterConverter = new TableConverter(new PaulBourkeTable()),
      cellAspect = 1,
      grayscaleFilters = Seq(new BrightnessFilter(30))
    )

    AsciiArtPipeline.render(fixture, settings).toText shouldBe
      """#*ahbpwmOQCJYzcu
        |bdqmZ0LCUXzvnrjt
        |mOQCJYzvuxrf/|(1
        |JUXcvnrft\(){[]-
        |vuxjt/|(1}]?_~>i
        |ft\(1{[?-+<>!I;,
        |){}]?_~>il;,"`..
        |?_+<i!I:,^'.....""".stripMargin
  }

  it should "render the seed 7 plasma at 40 columns" in {
    AsciiArtPipeline.render(Plasma.generate(7), RenderSettings(maxColumns = 40)).toText shouldBe
      """:-=+****+=-:....:-+*##%%%#**+===++***###
        |=+*****+=-::::-=+*#%@@%%#*+=====++*****+
        |*###**+=-::--=*#%@@@%%#*+=----==++++++=-
        |%%##*+=---=+*#%%@@%#*+=-::::--=++++==--:
        |%#*+=----=+*#%%%%#*=-:....:--=++++==---:
        |#+=-----=+*####*+=-:.  ..:-=+++++==-----
        |=-::::-=+*##**+=:..  ..:-++*****+======+
        |...::-=+****+=-..  .:-=+*####**++===++**
        | .:-=+****+=-:....:-+*#%%%%##*+====++**#
        |.:=+**#**+=-::::-=+*%%@@%%#*+======++***
        |=+*####*+=-----=*#%%@@%%#*+=-----=++++++
        |#%%%%#*+=---==+*#%@@%#*+=-:::::-==++++==
        |@@%%#+=----=+*#%%%%#*=-:....::-=+++++=--
        |@%#*=--:--=+*####*+=-:.  ..:-=++**++==--
        |#+=-::::-=+*****+=:..  ..:-+**##**++====""".stripMargin
  }
}
