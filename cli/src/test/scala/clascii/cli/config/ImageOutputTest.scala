package clascii.cli.config

import clascii.core.image.AsciiArt
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.io.{BufferedReader, File, FileReader}

class PathImageOutputTest extends AnyFlatSpec with Matchers {

  "PathImageOutput" should "correctly save ASCII art to a file" in {
    val asciiArt = new AsciiArt(10, 5, Array.fill(50)('x'))
    val tempFile = File.createTempFile("asciiArt", ".txt")
    val output = new PathImageOutput(tempFile.getAbsolutePath)

    output.save(asciiArt)

    val reader = new BufferedReader(new FileReader(tempFile))
    val content = Iterator.continually(reader.readLine()).takeWhile(_ != null).mkString("\n")
    reader.close()

    content shouldBe ("xxxxxxxxxx\n" * 5).trim
    tempFile.delete() // Clean up the temporary file
  }
}

class ConsoleImageOutputTest extends AnyFlatSpec with Matchers {

  "ConsoleImageOutput" should "correctly print ASCII art to the console" in {
    val asciiArt = new AsciiArt(10, 5, Array.fill(50)('x'))
    val output = new ConsoleImageOutput()

    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {
      //all printlns in this block will be redirected
      output.save(asciiArt)
    }
    stream.toString().trim shouldBe ("xxxxxxxxxx\n" * 5).trim

  }
}