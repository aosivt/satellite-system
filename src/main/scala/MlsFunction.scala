import org.apache.spark.sql.{SparkSession, functions}
import org.apache.spark.sql.expressions.UserDefinedFunction

class MlsFunction(spark: SparkSession, i: Integer) extends Serializable {
  def rotateStringUdf: UserDefinedFunction =
    functions.udf { str: String => MlsFunction.rotateString(str, i)
}
}
object MlsFunction{
  def rotateString(str: String, nRotations: Integer): String =
    str.substring(nRotations) + str.substring(0, nRotations)
}