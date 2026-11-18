package asciiart.cli

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.io.ByteArrayOutputStream

class MainTest extends AnyFlatSpec with Matchers {

  "Main" should "print a user error without a stack trace and exit with 1" in {
    val err = new ByteArrayOutputStream()

    val status = Console.withErr(err) {
      Main.run(Array("--table", "nope"))
    }

    status shouldBe 1
    err.toString.trim shouldBe "Error: Unknown table type"
  }
}
