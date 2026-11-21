package asciiart.core.table

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class TableNameMapperTest extends AnyFlatSpec with Matchers {

  "TableNameMapper" should "return a PaulBourkeTable for 'paulborkes'" in {
    val table = TableNameMapper("paulborkes")
    table shouldBe a[PaulBourkeTable]
  }

  it should "return a DefaultTable for 'default'" in {
    val table = TableNameMapper("default")
    table shouldBe a[DefaultTable]
  }

  it should "return a DefaultNonLinearTable for 'nonlinear-default'" in {
    val table = TableNameMapper("nonlinear-default")
    table shouldBe a[DefaultNonLinearTable]
  }

  it should "throw IllegalArgumentException for unknown table names" in {
    an[IllegalArgumentException] should be thrownBy TableNameMapper("unknown")
  }
}
