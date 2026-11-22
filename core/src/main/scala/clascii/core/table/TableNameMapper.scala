package clascii.core.table

object TableNameMapper {

  // A new table for every lookup, since a table's characters are a mutable array.
  private val tables: Seq[(String, () => Table)] = Seq(
    "default" -> (() => new DefaultTable()),
    "nonlinear-default" -> (() => new DefaultNonLinearTable()),
    "bourke" -> (() => new PaulBourkeTable())
  )

  /** The names of the built-in tables, in the order to offer them. */
  val names: Seq[String] = tables.map(_._1)

  def apply(tableName : String): Table = {
    val name = tableName.toLowerCase
    tables.collectFirst { case (`name`, table) => table() }
      .getOrElse(throw new IllegalArgumentException(s"Unknown table '$tableName': use ${names.mkString(", ")}"))
  }
}
