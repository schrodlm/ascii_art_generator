package asciiart.core.table

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers


class LinearTableTest extends AnyFlatSpec with Matchers {

  "LinearTable" should "correctly map values to characters" in {
    val chars = Array('a', 'b', 'c', 'd')
    val table = new LinearTable(chars)

    table.getChar(0) shouldBe 'a'         // Test lower boundary
    table.getChar(127) shouldBe 'b'       // Test middle value
    table.getChar(255) shouldBe 'd'       // Test upper boundary
  }

  it should "give every character an equal run of grey values" in {
    val table = new LinearTable(Array('a', 'b', 'c', 'd'))

    (0 to 255).groupBy(table.getChar).map { case (char, values) => char -> values.size } shouldBe
      Map('a' -> 64, 'b' -> 64, 'c' -> 64, 'd' -> 64)
    table.getChar(191) shouldBe 'c'
    table.getChar(192) shouldBe 'd'
  }

  it should "reject an empty character set" in {
    val error = the[IllegalArgumentException] thrownBy new LinearTable(Array.empty[Char])

    error.getMessage shouldBe "A character table needs at least one character"
  }

  "CustomTable" should "reject an empty character set" in {
    an[IllegalArgumentException] should be thrownBy new CustomTable(Array.empty[Char])
  }
}

class NonLinearTableTest extends AnyFlatSpec with Matchers {

  "NonLinearTable" should "correctly map values to characters" in {
    val chars = Array((85, 'a'), (170, 'b'), (255, 'c'))
    val table = new NonLinearTable(chars)

    table.getChar(0) shouldBe 'a'         // Test lower boundary
    table.getChar(85) shouldBe 'a'        // Test first border
    table.getChar(86) shouldBe 'b'        // Test just above first border
    table.getChar(170) shouldBe 'b'       // Test second border
    table.getChar(255) shouldBe 'c'       // Test upper boundary
  }

  it should "reject tables that would leave a grey without a character or a character unreachable" in {
    def error(chars: (Int, Char)*): String =
      (the[IllegalArgumentException] thrownBy new NonLinearTable(chars.toArray)).getMessage

    error() shouldBe "A character table needs at least one character"
    error((-1, 'a'), (255, 'b')) shouldBe "Grey limits must be between 0 and 255"
    error((100, 'a'), (256, 'b')) shouldBe "Grey limits must be between 0 and 255"
    error((200, 'a'), (100, 'b'), (255, 'c')) shouldBe "Grey limits must rise from one character to the next"
    error((100, 'a'), (100, 'b'), (255, 'c')) shouldBe "Grey limits must rise from one character to the next"
    error((100, 'a'), (200, 'b')) shouldBe "The last grey limit must be 255, so every grey has a character"
  }
}
