package clascii.core.table

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class TableNameMapperTest extends AnyFlatSpec with Matchers {

  "TableNameMapper" should "return a PaulBourkeTable for 'bourke'" in {
    val table = TableNameMapper("bourke")
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

  it should "name the built-in tables for an unknown name" in {
    the[IllegalArgumentException] thrownBy TableNameMapper("unknown") should have message
      "Unknown table 'unknown': use default, nonlinear-default, bourke"
  }

  it should "list the built-in tables in the order to offer them" in {
    TableNameMapper.names shouldBe Seq("default", "nonlinear-default", "bourke")
  }
}
