import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.api.java.UDF2
import org.apache.spark.sql.catalyst.expressions.NaNvl
import org.apache.spark.sql.functions.udf
import org.apache.spark.sql.types.DoubleType


object SparkUDFPlusOne extends App with Serializable {

  val spark: SparkSession = SparkSession.builder()
    .master("local")
    .appName("SparkByExamples.com")
    .getOrCreate()

  val javaUdf = udf(new UDF2[Seq[Double], Seq[Double], Double] {
    override def call(X:Seq[Double],Y:Seq[Double] ):Double = {
      val SumXi = X.sum
      val SumYi = Y.sum
      val SqrSumXi = X.map(x=>Math.pow(x,2)).sum
      val SumXi3 = X.map(x=>Math.pow(x,3)).sum
      val SumXi4 = X.map(x=>Math.pow(x,4)).sum
      val SqrSumYi = Y.map(y=>Math.pow(y,2)).sum
      val XiYiSum = X.zip(Y).map((l: (Double, Double)) => l._1 * l._2).sum
      val Xi2YiSum = X.zip(Y).map((l: (Double, Double)) => Math.pow(l._1,2) * l._2).sum

      val A = ((SumXi * SumYi) - (X.length*XiYiSum))/(Math.pow(SumXi,2)-(X.length*SqrSumXi))
      val B = (SumXi*XiYiSum - (SqrSumXi*SumYi))/(Math.pow(SumXi,2)-(X.length*SqrSumXi))

      println(s"$SqrSumXi a + $SumXi b + ${X.length} c = $SumYi" )
      println(s"$SumXi3 a + $SqrSumXi b + $SumXi c = $XiYiSum" )
      println(s"$SumXi4 a + $SumXi3 b + $SqrSumXi c = $Xi2YiSum" )

      val M = Array.ofDim[Double](3,3)
      M(0) = Array(SqrSumXi,SumXi,X.length)
      M(1) = Array(SumXi3,SqrSumXi,SumXi)
      M(2) = Array(SumXi4,SumXi3,SqrSumXi)

      val Ma = Array.ofDim[Double](3,3)
      Ma(0) = Array(SumYi,SumXi,X.length)
      Ma(1) = Array(XiYiSum,SqrSumXi,SumXi)
      Ma(2) = Array(Xi2YiSum,SumXi3,SqrSumXi)

      val Mb = Array.ofDim[Double](3,3)
      Mb(0) = Array(SqrSumXi,SumYi,X.length)
      Mb(1) = Array(SumXi3,XiYiSum,SumXi)
      Mb(2) = Array(SumXi4,Xi2YiSum,SqrSumXi)

      val Mc = Array.ofDim[Double](3,3)
      Mc(0) = Array(SqrSumXi,SumXi,SumYi)
      Mc(1) = Array(SumXi3,SqrSumXi,XiYiSum)
      Mc(2) = Array(SumXi4,SumXi3,Xi2YiSum)


      def cramer: (Int, Array[Array[Double]])=> Double = (i, m) => {
        m.length match {
          case 1 => m(0)(0)
          case _ =>
            val nextI = i+1
            val A:Array[Array[Double]] = m.drop(1).map(a=>{
              val template = a.take(i) ++ a.drop(i+1)
              template
            })
            val rootElement = (if (i%2==0)  m(0)(i) else - m(0)(i)) * cramer(0, A)
            rootElement + (if (nextI==m.length) 0 else cramer(nextI, m))
        }
      }

      val DM = cramer(0,M)
      val DMa = cramer(0,Ma)
      val DMb = cramer(0,Mb)
      val DMc = cramer(0,Mc)

      val a = DMa/DM
      val b = DMb/DM
      val c = DMc/DM

      val Yrg = X.map(x=>a*Math.pow(x,2) + b * x + c)
      Yrg.foreach(println)
      val Ysr = (1D/X.length)*SumYi
      val YsrY = Y.map(y=>y-Ysr)
      val YsrYSum = YsrY.sum
      val YsrY2 = YsrY.map(y=>Math.pow(y,2))
      val YsrYSum2 = YsrY2.sum
      val YrgY = Y.zip(Yrg).map(y=>y._1-y._2)
      val YrgY2 = YrgY.map(y=>Math.pow(y,2))
      val YrgY2Sum = YrgY2.sum
      //            index correlation
      val forRSearch: Double = (YrgY2Sum/YsrYSum2)
      val tt: Double = 1.0 - forRSearch
      val R = Math.sqrt(tt)
      //            index determination
      val R2 = Math.pow(R,2)

      val sumTest = (Y.zip(Yrg).map(y=>Math.abs(y._1-y._2)/y._1))
      //      Average approximation error
      val Asr = ((1D/X.length)*sumTest.sum) * 100

      R match {
        case Double.NaN => 0D
        case Double.NegativeInfinity => -100D
        case Double.PositiveInfinity => 100D
        case _ => R  }
    }
  }, DoubleType).asNondeterministic()
  spark.udf.register("javaUdf", javaUdf)

  val result = spark.sql("" +
//    "select rowId, projection, array(487648.0, geoTransform[1], geoTransform[2],6015073.0,geoTransform[4],geoTransform[5]) geoTransform, collect_list(regression) result from ("+
    "select rowId,colId, projection, geoTransform, javaUdf(array(cast(2016 as double),cast(2018 as double),cast(2020 as double))," +
      "array(S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE_data," +
            "S2A_MSIL1C_20180711T051651_N0206_R062_T45UVA_20180711T083042_SAFE_data, " +
            "S2B_MSIL1C_20200817T052649_N0209_R105_T45UVA_20200817T073431_SAFE_data)) regression" +
            " " +
    "from parquet.`/media/alex/058CFFE45C3C7827/maiskoe/project/S2A/*.parquet`" +
    " "
  )
//    +
//    ")" +
//    " group by rowId, projection, geoTransform")
//  result.show()
  result.write.parquet("/media/alex/058CFFE45C3C7827/maiskoe/project/regression")
}
