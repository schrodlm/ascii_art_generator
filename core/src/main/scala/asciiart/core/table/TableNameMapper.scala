package asciiart.core.table

object TableNameMapper {

  def apply(tableName : String): Table ={
    tableName.toLowerCase match {
      case "paulborkes" => new PaulBourkeTable()
      case "default" => new DefaultTable()
      case "nonlinear-default" => new DefaultNonLinearTable()
      case _ => throw new IllegalArgumentException("Unknown table type")
    }
  }
}
