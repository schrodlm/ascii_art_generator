package asciiart.cli.loading

import asciiart.cli.config.PathImageSource
import asciiart.core.image.RGBImage
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.awt.image.BufferedImage
import java.nio.file.Files
import javax.imageio.ImageIO

class SourceToRgbImageTest extends AnyFlatSpec with Matchers with BeforeAndAfterAll {

  private val dir = Files.createTempDirectory("asciiart-test")

  override def afterAll(): Unit = {
    dir.toFile.listFiles().foreach(_.delete())
    Files.delete(dir)
  }

  private def writeImage(name: String, format: String): String = {
    val file = dir.resolve(name).toFile
    ImageIO.write(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB), format, file)
    file.getPath
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

  it should "recognise the format from the contents, whatever the extension says" in {
    val files = Seq("x.PNG" -> "png", "x.jpeg" -> "jpeg", "x.JPG" -> "jpeg", "x.gif" -> "gif", "png-named.jpg" -> "png")

    for ((name, format) <- files) {
      val image = new SourceToRgbImage(PathImageSource(writeImage(name, format))).convert()

      withClue(name) {
        (image.getWidth(), image.getHeight()) shouldBe ((1, 1))
      }
    }
  }
}
