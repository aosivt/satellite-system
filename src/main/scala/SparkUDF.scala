import org.apache.spark.sql.functions.udf
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.{Row, SparkSession}


import scala.collection.mutable

object SparkUDF extends App{

  val spark: SparkSession = SparkSession.builder()
    .master("local")
    .appName("SparkByExamples.com")
    .getOrCreate()


//  Старт
  val result = spark.sqlContext.sql(
    "  select S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE.rowId, " +
    "   S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE.colId, " +
    "   S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE.width, " +
    "   S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE.height, " +
    "   S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE.projection, " +
    "   array(474530, S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE.geoTransform[1], S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE.geoTransform[2],6043279,S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE.geoTransform[4],S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE.geoTransform[5])geoTransform, " +
    "   S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE.data S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE_data, " +
    "   S2B_MSIL1C_20200817T052649_N0209_R105_T45UVA_20200817T073431_SAFE.data S2B_MSIL1C_20200817T052649_N0209_R105_T45UVA_20200817T073431_SAFE_data," +
    "   S2A_MSIL1C_20180711T051651_N0206_R062_T45UVA_20180711T083042_SAFE.data S2A_MSIL1C_20180711T051651_N0206_R062_T45UVA_20180711T083042_SAFE_data" +
      " from (" +
      "( " +
        "select " +
      " (case when indArray < 3 then rowId - 1 when 3 <= indArray and indArray < 6 then rowId when 6 <= indArray  then rowId + 1 end) rowId," +
      " (case when array_contains(Array(0,3,6),indArray) then colId - 1 when array_contains(Array(2,5,8),indArray) then colId + 1 else colId end)  colId, " +
      " width, height, projection, geoTransform, data from (" +
      " select *, array(" +
      " case when ((dataNIR[0]-dataRed[0]) / (dataNIR[0]+dataRed[0])) is null then 0 else ((dataNIR[0]-dataRed[0]) / (dataNIR[0]+dataRed[0])) end," +
      " case when ((dataNIR[1]-dataRed[1]) / (dataNIR[1]+dataRed[1])) is null then 0 else ((dataNIR[0]-dataRed[1]) / (dataNIR[0]+dataRed[1])) end," +
      " case when ((dataNIR[2]-dataRed[2]) / (dataNIR[2]+dataRed[2])) is null then 0 else ((dataNIR[0]-dataRed[2]) / (dataNIR[0]+dataRed[2])) end," +
      " case when ((dataNIR[3]-dataRed[3]) / (dataNIR[3]+dataRed[3])) is null then 0 else ((dataNIR[0]-dataRed[3]) / (dataNIR[0]+dataRed[3])) end," +
      " case when ((dataNIR[4]-dataRed[4]) / (dataNIR[4]+dataRed[4])) is null then 0 else ((dataNIR[0]-dataRed[4]) / (dataNIR[0]+dataRed[4])) end," +
      " case when ((dataNIR[5]-dataRed[5]) / (dataNIR[5]+dataRed[5])) is null then 0 else ((dataNIR[0]-dataRed[5]) / (dataNIR[0]+dataRed[5])) end," +
      " case when ((dataNIR[6]-dataRed[6]) / (dataNIR[6]+dataRed[6])) is null then 0 else ((dataNIR[0]-dataRed[6]) / (dataNIR[0]+dataRed[6])) end," +
      " case when ((dataNIR[7]-dataRed[7]) / (dataNIR[7]+dataRed[7])) is null then 0 else ((dataNIR[0]-dataRed[7]) / (dataNIR[0]+dataRed[7])) end," +
      " case when ((dataNIR[8]-dataRed[8]) / (dataNIR[8]+dataRed[8])) is null then 0 else ((dataNIR[0]-dataRed[8]) / (dataNIR[0]+dataRed[8])) end" +
      " ) ndvi" +
      " from parquet.`/media/alex/058CFFE45C3C7827/maiskoe/2016/лето/parquet/S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE/*.parquet` )" +
      " lateral view posexplode(ndvi) results AS indArray, data " +
      " where (geoTransform[0] + ((colId + 1) * geoTransform[1]) + ((rowId + 1) * geoTransform[2])) between 474530 and 489963 " +
      " and   (geoTransform[3] + ((colId + 1) * geoTransform[4]) + ((rowId + 1) * geoTransform[5])) between 6028362 and 6043279)  " +
      ") S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE " +
      "inner join " +
      " (" +
      " select " +
      " (case when indArray < 3 then rowId - 1 when 3 <= indArray and indArray < 6 then rowId when 6 <= indArray  then rowId + 1 end) rowId," +
      " (case when array_contains(Array(0,3,6),indArray) then colId - 1 when array_contains(Array(2,5,8),indArray) then colId + 1 else colId end)  colId, " +
      " width, height, projection, geoTransform, data from (" +
      " select *, array(" +
      " case when ((dataNIR[0]-dataRed[0]) / (dataNIR[0]+dataRed[0])) is null then 0 else ((dataNIR[0]-dataRed[0]) / (dataNIR[0]+dataRed[0])) end," +
      " case when ((dataNIR[1]-dataRed[1]) / (dataNIR[1]+dataRed[1])) is null then 0 else ((dataNIR[0]-dataRed[1]) / (dataNIR[0]+dataRed[1])) end," +
      " case when ((dataNIR[2]-dataRed[2]) / (dataNIR[2]+dataRed[2])) is null then 0 else ((dataNIR[0]-dataRed[2]) / (dataNIR[0]+dataRed[2])) end," +
      " case when ((dataNIR[3]-dataRed[3]) / (dataNIR[3]+dataRed[3])) is null then 0 else ((dataNIR[0]-dataRed[3]) / (dataNIR[0]+dataRed[3])) end," +
      " case when ((dataNIR[4]-dataRed[4]) / (dataNIR[4]+dataRed[4])) is null then 0 else ((dataNIR[0]-dataRed[4]) / (dataNIR[0]+dataRed[4])) end," +
      " case when ((dataNIR[5]-dataRed[5]) / (dataNIR[5]+dataRed[5])) is null then 0 else ((dataNIR[0]-dataRed[5]) / (dataNIR[0]+dataRed[5])) end," +
      " case when ((dataNIR[6]-dataRed[6]) / (dataNIR[6]+dataRed[6])) is null then 0 else ((dataNIR[0]-dataRed[6]) / (dataNIR[0]+dataRed[6])) end," +
      " case when ((dataNIR[7]-dataRed[7]) / (dataNIR[7]+dataRed[7])) is null then 0 else ((dataNIR[0]-dataRed[7]) / (dataNIR[0]+dataRed[7])) end," +
      " case when ((dataNIR[8]-dataRed[8]) / (dataNIR[8]+dataRed[8])) is null then 0 else ((dataNIR[0]-dataRed[8]) / (dataNIR[0]+dataRed[8])) end" +
      " ) ndvi" +
      " from parquet.`/media/alex/058CFFE45C3C7827/maiskoe/2020/лето/parquet/S2B_MSIL1C_20200817T052649_N0209_R105_T45UVA_20200817T073431_SAFE/*.parquet` ) " +
      " lateral view posexplode(ndvi) results AS indArray, data " +
      " where (geoTransform[0] + ((colId + 1) * geoTransform[1]) + ((rowId + 1) * geoTransform[2])) between 474530 and 489963 " +
      " and   (geoTransform[3] + ((colId + 1) * geoTransform[4]) + ((rowId + 1) * geoTransform[5])) between 6028362 and 6043279  " +
      " ) S2B_MSIL1C_20200817T052649_N0209_R105_T45UVA_20200817T073431_SAFE " +
      " on " +
      "(S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE.geoTransform[0] + ((S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE.colId + 1) * S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE.geoTransform[1]) + ((S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE.rowId + 1) * S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE.geoTransform[2])) = " +
      "(S2B_MSIL1C_20200817T052649_N0209_R105_T45UVA_20200817T073431_SAFE.geoTransform[0] + ((S2B_MSIL1C_20200817T052649_N0209_R105_T45UVA_20200817T073431_SAFE.colId + 1) * S2B_MSIL1C_20200817T052649_N0209_R105_T45UVA_20200817T073431_SAFE.geoTransform[1]) + ((S2B_MSIL1C_20200817T052649_N0209_R105_T45UVA_20200817T073431_SAFE.rowId + 1) * S2B_MSIL1C_20200817T052649_N0209_R105_T45UVA_20200817T073431_SAFE.geoTransform[2])) and " +
      "(S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE.geoTransform[3] + ((S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE.colId + 1) * S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE.geoTransform[4]) + ((S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE.rowId + 1) * S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE.geoTransform[5])) = " +
      "(S2B_MSIL1C_20200817T052649_N0209_R105_T45UVA_20200817T073431_SAFE.geoTransform[3] + ((S2B_MSIL1C_20200817T052649_N0209_R105_T45UVA_20200817T073431_SAFE.colId + 1) * S2B_MSIL1C_20200817T052649_N0209_R105_T45UVA_20200817T073431_SAFE.geoTransform[4]) + ((S2B_MSIL1C_20200817T052649_N0209_R105_T45UVA_20200817T073431_SAFE.rowId + 1) * S2B_MSIL1C_20200817T052649_N0209_R105_T45UVA_20200817T073431_SAFE.geoTransform[5]))"+
      "inner join " +
        " (" +
        " select " +
        " (case when indArray < 3 then rowId - 1 when 3 <= indArray and indArray < 6 then rowId when 6 <= indArray  then rowId + 1 end) rowId," +
        " (case when array_contains(Array(0,3,6),indArray) then colId - 1 when array_contains(Array(2,5,8),indArray) then colId + 1 else colId end)  colId, " +
        " width, height, projection, geoTransform, data from (" +
        " select *, array(" +
        " case when ((dataNIR[0]-dataRed[0]) / (dataNIR[0]+dataRed[0])) is null then 0 else ((dataNIR[0]-dataRed[0]) / (dataNIR[0]+dataRed[0])) end," +
        " case when ((dataNIR[1]-dataRed[1]) / (dataNIR[1]+dataRed[1])) is null then 0 else ((dataNIR[0]-dataRed[1]) / (dataNIR[0]+dataRed[1])) end," +
        " case when ((dataNIR[2]-dataRed[2]) / (dataNIR[2]+dataRed[2])) is null then 0 else ((dataNIR[0]-dataRed[2]) / (dataNIR[0]+dataRed[2])) end," +
        " case when ((dataNIR[3]-dataRed[3]) / (dataNIR[3]+dataRed[3])) is null then 0 else ((dataNIR[0]-dataRed[3]) / (dataNIR[0]+dataRed[3])) end," +
        " case when ((dataNIR[4]-dataRed[4]) / (dataNIR[4]+dataRed[4])) is null then 0 else ((dataNIR[0]-dataRed[4]) / (dataNIR[0]+dataRed[4])) end," +
        " case when ((dataNIR[5]-dataRed[5]) / (dataNIR[5]+dataRed[5])) is null then 0 else ((dataNIR[0]-dataRed[5]) / (dataNIR[0]+dataRed[5])) end," +
        " case when ((dataNIR[6]-dataRed[6]) / (dataNIR[6]+dataRed[6])) is null then 0 else ((dataNIR[0]-dataRed[6]) / (dataNIR[0]+dataRed[6])) end," +
        " case when ((dataNIR[7]-dataRed[7]) / (dataNIR[7]+dataRed[7])) is null then 0 else ((dataNIR[0]-dataRed[7]) / (dataNIR[0]+dataRed[7])) end," +
        " case when ((dataNIR[8]-dataRed[8]) / (dataNIR[8]+dataRed[8])) is null then 0 else ((dataNIR[0]-dataRed[8]) / (dataNIR[0]+dataRed[8])) end" +
        " ) ndvi" +
        " from parquet.`/media/alex/058CFFE45C3C7827/maiskoe/2018/лето/parquet/S2A_MSIL1C_20180711T051651_N0206_R062_T45UVA_20180711T083042_SAFE/*.parquet` ) " +
        " lateral view posexplode(ndvi) results AS indArray, data " +
        " where (geoTransform[0] + ((colId + 1) * geoTransform[1]) + ((rowId + 1) * geoTransform[2])) between 474530 and 489963 " +
        " and   (geoTransform[3] + ((colId + 1) * geoTransform[4]) + ((rowId + 1) * geoTransform[5])) between 6028362 and 6043279  " +
        " ) S2A_MSIL1C_20180711T051651_N0206_R062_T45UVA_20180711T083042_SAFE " +
        " on " +
        "(S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE.geoTransform[0] + ((S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE.colId + 1) * S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE.geoTransform[1]) + ((S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE.rowId + 1) * S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE.geoTransform[2])) = " +
        "(S2A_MSIL1C_20180711T051651_N0206_R062_T45UVA_20180711T083042_SAFE.geoTransform[0] + ((S2A_MSIL1C_20180711T051651_N0206_R062_T45UVA_20180711T083042_SAFE.colId + 1) * S2A_MSIL1C_20180711T051651_N0206_R062_T45UVA_20180711T083042_SAFE.geoTransform[1]) + ((S2A_MSIL1C_20180711T051651_N0206_R062_T45UVA_20180711T083042_SAFE.rowId + 1) * S2A_MSIL1C_20180711T051651_N0206_R062_T45UVA_20180711T083042_SAFE.geoTransform[2])) and " +
        "(S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE.geoTransform[3] + ((S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE.colId + 1) * S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE.geoTransform[4]) + ((S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE.rowId + 1) * S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE.geoTransform[5])) = " +
        "(S2A_MSIL1C_20180711T051651_N0206_R062_T45UVA_20180711T083042_SAFE.geoTransform[3] + ((S2A_MSIL1C_20180711T051651_N0206_R062_T45UVA_20180711T083042_SAFE.colId + 1) * S2A_MSIL1C_20180711T051651_N0206_R062_T45UVA_20180711T083042_SAFE.geoTransform[4]) + ((S2A_MSIL1C_20180711T051651_N0206_R062_T45UVA_20180711T083042_SAFE.rowId + 1) * S2A_MSIL1C_20180711T051651_N0206_R062_T45UVA_20180711T083042_SAFE.geoTransform[5]))"
    //      ") "
  )
//  result.printSchema()
////
////  result.show(10)
  result.write.parquet("/media/alex/058CFFE45C3C7827/maiskoe/project/S2A")
//  spark.sql("select * from parquet.`/media/alex/058CFFE45C3C7827/maiskoe/project/S2A/*.parquet`").toDF().createOrReplaceTempView("template")
//  spark.sql("select plusOne(rowId) t from template").show(10)

}
