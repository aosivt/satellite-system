package org.satellite.system.web

import akka.actor.{ActorRef, Props}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpHeader, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.settings.RoutingSettings
import org.apache.spark.sql.SparkSession
import org.satellite.system.core.Application
import spray.json.{DefaultJsonProtocol, JsString, JsValue, jsonWriter}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport

import scala.collection.immutable.Seq
import org.apache.spark.sql.Encoders
import org.satellite.system.Main.materializer.executionContext
import spray.json.DefaultJsonProtocol.{CharJsonFormat, JsValueFormat, RootJsArrayFormat, StringJsonFormat, arrayFormat}

import scala.concurrent.Future



final case class Order(rowId: Int, projection: String, geoTransform: scala.Seq[Double], result: scala.Seq[Double])

// collect your json format instances into a support trait:
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val orderFormat = jsonFormat4(Order) // contains List[Item]
}

/**
  * This is where you define your XHR routes.
  *
  * @param application an Application
  */
class APIRoutes(application: Application, spark: SparkSession, usersSocket: Array[ActorRef]) extends JsonSupport{

  implicit val settings: RoutingSettings = RoutingSettings.apply(application.config)

  def testSpark(): Future[Array[Order]] = Future {
    val testChemaDf = spark.sqlContext.sql(
      "select rowId, projection, array(487648, geoTransform[1], geoTransform[2],6015073,geoTransform[4],geoTransform[5]), collect_list(data) result from (" +
      " select  " +
//        " (geoTransform[0] + ((colId + 1) * geoTransform[1]) + ((rowId + 1) * geoTransform[2])) value," +
//        " (geoTransform[3] + ((colId + 1) * geoTransform[4]) + ((rowId + 1) * geoTransform[5])) value2," +
//        Y_geo = GT(3) + X_pixel * GT(4) + Y_line * GT(5)
      " (case when indArray < 3 then rowId - 1 when 3 <= indArray and indArray < 6 then rowId when 6 <= indArray  then rowId + 1 end) rowId," +
      " (case when array_contains(Array(0,3,6),indArray) then colId - 1 when array_contains(Array(2,5,8),indArray) then colId + 1 else colId end)  colId, " +
      " width, height, projection, geoTransform, data, projection " +
      " from parquet.`/media/alex/058CFFE45C3C7827/maiskoe/2016/лето/parquet/S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE/*.parquet` " +
      " lateral view posexplode(dataBlue) results AS indArray, data " +
        " where (geoTransform[0] + ((colId + 1) * geoTransform[1]) + ((rowId + 1) * geoTransform[2])) between 487648 and 490195 " +
        " and   (geoTransform[3] + ((colId + 1) * geoTransform[4]) + ((rowId + 1) * geoTransform[5])) between 6013091 and 6015073  " +
      ") as r" +

//        " WHERE  colId + 1 between ((490195 - geoTransform[0])/geoTransform[1]) and ((487648 - geoTransform[0])/geoTransform[1])" +
//        " AND    rowId + 1 between ((6015073 - geoTransform[3])/geoTransform[5]) and ((6013091 - geoTransform[3])/geoTransform[5]) " +
      " group by rowId, projection, geoTransform" +
//      " limit 20" +
      " ")

//    testChemaDf.printSchema()
//    testChemaDf.show(20)
//    val df = spark.sqlContext.sql("select rowId, projection, geoTransform, result " +
//                                    "as result from parquet.`/media/alex/058CFFE45C3C7827/shahta12/2016/лето/parquet/S2A_MSIL1C_20160611T051652_N0202_R062_T45UVV_20160611T051654_SAFE/*.parquet`")
//    df.show

    testChemaDf.collect().map(m=>Order(m.getInt(0),m.getString(1),m.getSeq[Double](2),m.getSeq[Double](3)))
  }

  def routes: Route = {
    pathPrefix("api") {
      respondWithHeader(RawHeader("Access-Control-Allow-Origin", "*")) {
        Route.seal(concat(
          get {
            pathPrefix("get") {
              val test = testSpark()
             onComplete(test){
               result=>complete(result)
             }
            }
          },
          post {
            entity(as[JsValue]) { json =>
              complete(JsString("OK POST"))
            }
          },
          put {
            entity(as[JsValue]) { json =>
              complete(JsString("OK PUT"))
            }
          },
          delete {
            complete(StatusCodes.OK)
          },
          options {
            val corsHeaders: Seq[HttpHeader] = Seq(
              RawHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS"),
              RawHeader("Access-Control-Allow-Headers", "Content-Type")
            )
            respondWithHeaders(corsHeaders) {
              complete(StatusCodes.OK)
            }
          }
        ))
      }
    }
  }

  case class PlotlyData(rowId: Int, result: Array[Double])

  object PlotlyDataEncoders {
    implicit def PlotlyDataEncoder: org.apache.spark.sql.Encoder[String] =
      org.apache.spark.sql.Encoders.kryo[String]
  }

}
