package asciiart.core.pipeline

import asciiart.core.filter.BrightnessFilter
import asciiart.core.image.RGBImage
import asciiart.core.table.{DefaultTable, PaulBorkesTable}
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

  "The pipeline" should "render the fixture with the default table" in {
    val art = AsciiArtPipeline.render(fixture, RenderSettings(table = new DefaultTable()))

    art.toText shouldBe
      """@@@@@%%%%%####**
        |@@%%%%####*****+
        |%%#####****+++++
        |###*****++++====
        |****+++++====---
        |*++++=====----::
        |++====-----::::.
        |===----:::::....""".stripMargin
  }

  it should "render the fixture with the Paul Borkes table and a brightness filter" in {
    val settings = RenderSettings(
      table = new PaulBorkesTable(),
      grayscaleFilters = Seq(new BrightnessFilter(30))
    )

    AsciiArtPipeline.render(fixture, settings).toText shouldBe
      """#*ahbpqmO0LJYXcu
        |kdqwZ0LCUXzvnxjt
        |mOQLJYXcunrf/\(1
        |CUXzvnrjt\|){}]-
        |cuxrf/\(1{[?_+<i
        |jt\|){[]-+~>il;:
        |(1}[?_+<i!I:"^'.
        |]-+~>!I;,"`.....""".stripMargin
  }
}
