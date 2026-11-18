package asciiart.cli.loading

import asciiart.cli.config.PathImageSource
import asciiart.core.image.RGBImage
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.nio.file.Files

class SourceToRgbImageTest extends AnyFlatSpec with Matchers with BeforeAndAfterAll {

  private val dir = Files.createTempDirectory("asciiart-test")

  override def afterAll(): Unit = {
    dir.toFile.listFiles().foreach(_.delete())
    Files.delete(dir)
  }

  "SourceToRgbImage" should "load the image through the injected loading strategy" in {
    val image = new RGBImage(1, 1, Array(0))
    val strategy = new ImageLoadingStrategy {
      override def load(): RGBImage = image
    }
    val converter = new SourceToRgbImage(PathImageSource("test.png"), Some(strategy))

    converter.convert() shouldBe image
  }

  it should "throw IllegalArgumentException for a file that is not an image" in {
    val path = dir.resolve("notes.png")
    Files.write(path, "not an image".getBytes)

    val error = the[IllegalArgumentException] thrownBy new SourceToRgbImage(PathImageSource(path.toString)).convert()

    error.getMessage shouldBe s"'$path' is not an image in a supported format (PNG, JPEG, GIF, BMP or TIFF)"
  }
}
