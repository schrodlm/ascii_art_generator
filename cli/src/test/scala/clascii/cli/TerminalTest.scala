package clascii.cli

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class TerminalTest extends AnyFlatSpec with Matchers {

  "Terminal" should "read the columns from the answer of stty size" in {
    Terminal.columnsFromSize("24 80") shouldBe Some(80)
    Terminal.columnsFromSize("50 213\n") shouldBe Some(213)
  }

  it should "give no width for an answer it cannot read" in {
    for (answer <- Seq("", "80", "rows cols", "24 0", "stty: /dev/tty: No such device or address")) {
      withClue(answer) {
        Terminal.columnsFromSize(answer) shouldBe None
      }
    }
  }
}
