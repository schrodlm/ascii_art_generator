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
}
