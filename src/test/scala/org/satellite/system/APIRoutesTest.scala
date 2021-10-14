package org.satellite.system

import akka.actor.{ActorRef, Props}
import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, MediaTypes, MessageEntity, StatusCodes}

import scala.concurrent.duration._
import scala.util.{Failure, Success}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestProbe
import akka.util.{ByteString, Timeout}
import org.apache.spark.sql.{Column, SparkSession, functions}
import org.satellite.system.core.Application
import org.satellite.system.web.{APIRoutes, SocketActor}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import spray.json.DefaultJsonProtocol._
import spray.json.{JsString, RootJsonFormat, jsonWriter}


import scala.collection.mutable

case class User(name: String, age: Int)
object User{
  implicit val userJsonFormat: RootJsonFormat[User] = jsonFormat2(User.apply)
}

class APIRoutesTest extends AnyWordSpec with Serializable with Matchers with ScalaFutures with ScalatestRouteTest{
//  extends AnyWordSpec with Matchers with ScalatestRouteTest {

  val request = HttpRequest(uri = "/api/get")

  val postRequest = HttpRequest(uri = "/api")

  val usersSocket: Array[ActorRef] = Array()

  implicit val app: Application = Application()
  implicit val spark: SparkSession = SparkSession.builder
    .master("local")
    .getOrCreate
  // the Akka HTTP route testkit does not yet support a typed actor system (https://github.com/akka/akka-http/issues/2036)
  // so we have to adapt for now
  lazy val testKit = ActorTestKit()
  implicit def typedSystem = testKit.system
  override def createActorSystem(): akka.actor.ActorSystem = testKit.system.classicSystem
  // Here we need to implement all the abstract members of UserRoutes.
  // We use the real UserRegistryActor to test it while we hit the Routes,
  // but we could "mock" it by implementing it in-place or by using a TestProbe
  // created with testKit.createTestProbe()
  val apiRoutes = new APIRoutes(app, spark, usersSocket)
  lazy val routes = apiRoutes.routes
  val user = User("Kapi", 42)

  "The service" should {
    "Test API" in {
      request ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===("\"OK GET\"")
      }
    }

    "Test save parquet spark action" in {
      val ds1 = spark.sqlContext.sql(
      "select rowId, projection, array(487648.0, geoTransform[1], geoTransform[2],6015073.0,geoTransform[4],geoTransform[5]) geoTransform, collect_list(data) result from (" +
        " select " +
        " (case when indArray < 3 then rowId - 1 when 3 <= indArray and indArray < 6 then rowId when 6 <= indArray  then rowId + 1 end) rowId," +
        " (case when array_contains(Array(0,3,6),indArray) then colId - 1 when array_contains(Array(2,5,8),indArray) then colId + 1 else colId end)  colId, " +
        " width, height, projection, geoTransform, data, projection from (" +
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
        " from parquet.`/media/alex/058CFFE45C3C7827/maiskoe/2016/лето/parquet/S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE/*.parquet`) " +
        " lateral view posexplode(ndvi) results AS indArray, data " +
        " where (geoTransform[0] + ((colId + 1) * geoTransform[1]) + ((rowId + 1) * geoTransform[2])) between 487648 and 490195 " +
        " and   (geoTransform[3] + ((colId + 1) * geoTransform[4]) + ((rowId + 1) * geoTransform[5])) between 6013091 and 6015073  " +
        " ) " +
        " group by rowId, projection, geoTransform" )

      ds1.write.parquet("/media/alex/058CFFE45C3C7827/maiskoe/2016/лето/parquet/ndvi/S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE")
    }
    "Test mls with save parquet spark action" in {
      val result = spark.sqlContext.sql(
          " select mls(array(2016.0,2020,0),array(ds1.data,ds2.data)) mls, * from (" +
          "( select " +
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
          " where (geoTransform[0] + ((colId + 1) * geoTransform[1]) + ((rowId + 1) * geoTransform[2])) between 487648 and 490195 " +
          " and   (geoTransform[3] + ((colId + 1) * geoTransform[4]) + ((rowId + 1) * geoTransform[5])) between 6013091 and 6015073  " +
          " ) ds1 inner join " +
            "" +
            " (select " +
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
            " where (geoTransform[0] + ((colId + 1) * geoTransform[1]) + ((rowId + 1) * geoTransform[2])) between 487648 and 490195 " +
            " and   (geoTransform[3] + ((colId + 1) * geoTransform[4]) + ((rowId + 1) * geoTransform[5])) between 6013091 and 6015073  " +
            " ) ds2" +
            " (select " +
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
            " where (geoTransform[0] + ((colId + 1) * geoTransform[1]) + ((rowId + 1) * geoTransform[2])) between 487648 and 490195 " +
            " and   (geoTransform[3] + ((colId + 1) * geoTransform[4]) + ((rowId + 1) * geoTransform[5])) between 6013091 and 6015073  " +
            " ) ds3" +
            " on " +
            "(ds1.geoTransform[0] + ((ds1.colId + 1) * ds1.geoTransform[1]) + ((ds1.rowId + 1) * ds1.geoTransform[2])) = " +
            "(ds2.geoTransform[0] + ((ds2.colId + 1) * ds2.geoTransform[1]) + ((ds2.rowId + 1) * ds2.geoTransform[2])) and " +
            "(ds1.geoTransform[0] + ((ds1.colId + 1) * ds1.geoTransform[1]) + ((ds1.rowId + 1) * ds1.geoTransform[2])) = " +
            "(ds3.geoTransform[0] + ((ds3.colId + 1) * ds3.geoTransform[1]) + ((ds3.rowId + 1) * ds3.geoTransform[2])) and " +
            "(ds1.geoTransform[3] + ((ds1.colId + 1) * ds1.geoTransform[4]) + ((ds1.rowId + 1) * ds1.geoTransform[5])) = " +
            "(ds2.geoTransform[3] + ((ds2.colId + 1) * ds2.geoTransform[4]) + ((ds2.rowId + 1) * ds2.geoTransform[5]))" +
            ") "
         )


//      val ds2 = spark.sqlContext.sql(
//        "" +
//          " (select " +
//          " (case when indArray < 3 then rowId - 1 when 3 <= indArray and indArray < 6 then rowId when 6 <= indArray  then rowId + 1 end) rowId," +
//          " (case when array_contains(Array(0,3,6),indArray) then colId - 1 when array_contains(Array(2,5,8),indArray) then colId + 1 else colId end)  colId, " +
//          " width, height, projection, geoTransform, data from (" +
//          " select *, array(" +
//          " case when ((dataNIR[0]-dataRed[0]) / (dataNIR[0]+dataRed[0])) is null then 0 else ((dataNIR[0]-dataRed[0]) / (dataNIR[0]+dataRed[0])) end," +
//          " case when ((dataNIR[1]-dataRed[1]) / (dataNIR[1]+dataRed[1])) is null then 0 else ((dataNIR[0]-dataRed[1]) / (dataNIR[0]+dataRed[1])) end," +
//          " case when ((dataNIR[2]-dataRed[2]) / (dataNIR[2]+dataRed[2])) is null then 0 else ((dataNIR[0]-dataRed[2]) / (dataNIR[0]+dataRed[2])) end," +
//          " case when ((dataNIR[3]-dataRed[3]) / (dataNIR[3]+dataRed[3])) is null then 0 else ((dataNIR[0]-dataRed[3]) / (dataNIR[0]+dataRed[3])) end," +
//          " case when ((dataNIR[4]-dataRed[4]) / (dataNIR[4]+dataRed[4])) is null then 0 else ((dataNIR[0]-dataRed[4]) / (dataNIR[0]+dataRed[4])) end," +
//          " case when ((dataNIR[5]-dataRed[5]) / (dataNIR[5]+dataRed[5])) is null then 0 else ((dataNIR[0]-dataRed[5]) / (dataNIR[0]+dataRed[5])) end," +
//          " case when ((dataNIR[6]-dataRed[6]) / (dataNIR[6]+dataRed[6])) is null then 0 else ((dataNIR[0]-dataRed[6]) / (dataNIR[0]+dataRed[6])) end," +
//          " case when ((dataNIR[7]-dataRed[7]) / (dataNIR[7]+dataRed[7])) is null then 0 else ((dataNIR[0]-dataRed[7]) / (dataNIR[0]+dataRed[7])) end," +
//          " case when ((dataNIR[8]-dataRed[8]) / (dataNIR[8]+dataRed[8])) is null then 0 else ((dataNIR[0]-dataRed[8]) / (dataNIR[0]+dataRed[8])) end" +
//          " ) ndvi" +
//          " from parquet.`/media/alex/058CFFE45C3C7827/maiskoe/2020/лето/parquet/S2B_MSIL1C_20200817T052649_N0209_R105_T45UVA_20200817T073431_SAFE/*.parquet` ) " +
//          " lateral view posexplode(ndvi) results AS indArray, data " +
//          " where (geoTransform[0] + ((colId + 1) * geoTransform[1]) + ((rowId + 1) * geoTransform[2])) between 487648 and 490195 " +
//          " and   (geoTransform[3] + ((colId + 1) * geoTransform[4]) + ((rowId + 1) * geoTransform[5])) between 6013091 and 6015073  " +
//          " ) as ds2" )

//      val result = ds1.as("ds1").join(ds2.as("ds2")).where(
//        "(ds1.geoTransform[0] + ((ds1.colId + 1) * ds1.geoTransform[1]) + ((ds1.rowId + 1) * ds1.geoTransform[2])) = " +
//          "(ds2.geoTransform[0] + ((ds2.colId + 1) * ds2.geoTransform[1]) + ((ds2.rowId + 1) * ds2.geoTransform[2])) and " +
//          "(ds1.geoTransform[3] + ((ds1.colId + 1) * ds1.geoTransform[4]) + ((ds1.rowId + 1) * ds1.geoTransform[5])) = " +
//          "(ds2.geoTransform[3] + ((ds2.colId + 1) * ds2.geoTransform[4]) + ((ds2.rowId + 1) * ds2.geoTransform[5]))"
//      )
//        .createOrReplaceTempView("template")
//        .select(ds1.col("rowId"),ds1.col("colId"),ds1.col("geoTransform"),ds1.col("projection"),
//          mls(Seq[Double](2016.0,2017.0),Seq[Column](ds1.col("ds1.data"),ds1.col("ds2.data"))))
//        .sqlContext.sql("array(ds1.colId, ds2.colId) dataY, array(ds1.data,ds2.data) dataZ")
//        .col("ds1.data").as("ds1.2016_data")
//        .col("ds2.data").as("ds1.2020_data")
//
//      val result = spark.sqlContext.sql("select *, mls(array(2016.0,2020.0),array(ds1.data,ds2.data)) mls  from template")
//        result.show(10)
//      result.printSchema()

      result.write.parquet("/media/alex/058CFFE45C3C7827/maiskoe/2020/лето/parquet/mls/S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE")

    }
    "Test other type research" in {
      //      val ds1 = spark.sqlContext.sql(
      //      "select rowId, projection, array(487648, geoTransform[1], geoTransform[2],6015073,geoTransform[4],geoTransform[5]), collect_list(data) result from " +
      //      " (" +
      //      " select dataNIR.colId colId, dataNIR.rowId rowId, dataNIR.projection projection, dataNIR.geoTransform geoTransform, ((dataNIR.data-dataRed.data) / (dataNIR.data+dataRed.data)) data  from" +
      //      " (select  " +
      //      " (case when indArray < 3 then rowId - 1 when 3 <= indArray and indArray < 6 then rowId when 6 <= indArray  then rowId + 1 end) rowId," +
      //      " (case when array_contains(Array(0,3,6),indArray) then colId - 1 when array_contains(Array(2,5,8),indArray) then colId + 1 else colId end)  colId, " +
      //      " width, height, projection, geoTransform, data, projection " +
      //      " from parquet.`/media/alex/058CFFE45C3C7827/maiskoe/2016/лето/parquet/S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE/*.parquet` " +
      //      " lateral view posexplode(dataNIR) results AS indArray, data " +
      //      " where (geoTransform[0] + ((colId + 1) * geoTransform[1]) + ((rowId + 1) * geoTransform[2])) between 487648 and 490195 " +
      //      " and   (geoTransform[3] + ((colId + 1) * geoTransform[4]) + ((rowId + 1) * geoTransform[5])) between 6013091 and 6015073  " +
      //      " ) as dataNIR " +
      //      " inner join " +
      //      " (select  " +
      //      " (case when indArray < 3 then rowId - 1 when 3 <= indArray and indArray < 6 then rowId when 6 <= indArray  then rowId + 1 end) rowId," +
      //      " (case when array_contains(Array(0,3,6),indArray) then colId - 1 when array_contains(Array(2,5,8),indArray) then colId + 1 else colId end)  colId, " +
      //      " width, height, projection, geoTransform, data, projection " +
      //      " from parquet.`/media/alex/058CFFE45C3C7827/maiskoe/2016/лето/parquet/S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE/*.parquet` " +
      //      " lateral view posexplode(dataRed) results AS indArray, data " +
      //      " where (geoTransform[0] + ((colId + 1) * geoTransform[1]) + ((rowId + 1) * geoTransform[2])) between 487648 and 490195 " +
      //      " and   (geoTransform[3] + ((colId + 1) * geoTransform[4]) + ((rowId + 1) * geoTransform[5])) between 6013091 and 6015073  " +
      //      " ) as dataRed " +
      //      " on dataNIR.colId = dataRed.colId and dataNIR.rowId = dataRed.rowId) as S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE" +
      //      " group by rowId, projection, geoTransform" +
      //      " ")

    }

    "Test API inside" in {
      val jsonRequest = ByteString(
        s"""
           |{
           |    "name":"test"
           |}
        """.stripMargin)
      val postRequest = HttpRequest(
        HttpMethods.POST,
        uri = "/api",
        entity = HttpEntity(MediaTypes.`application/json`, jsonRequest))
      val putRequest = HttpRequest(
        HttpMethods.PUT,
        uri = "/api",
        entity = HttpEntity(MediaTypes.`application/json`, jsonRequest))

      // tests:
      Get("/api/get") ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        responseAs[String] shouldEqual ("\"OK GET\"")
      }
      postRequest ~> routes ~> check {
        status should ===(StatusCodes.OK)

        contentType should ===(ContentTypes.`application/json`)
        responseAs[String] shouldEqual ("\"OK POST\"")
      }
      putRequest ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        responseAs[String] shouldEqual ("\"OK PUT\"")
      }
      Delete("/api/") ~> routes ~> check {
        status should ===(StatusCodes.OK)
      }

    }
  }
}