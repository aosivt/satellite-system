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

    val ds1 = spark.sqlContext.sql(
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


      "select rowId, projection, array(487648, geoTransform[1], geoTransform[2],6015073,geoTransform[4],geoTransform[5]), collect_list(data) result from (" +
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

    ds1.printSchema()
    ds1.show(5)
    ds1.collect().map(m=>Order(m.getInt(0),m.getString(1),m.getSeq[Double](2),m.getSeq[Double](3)))
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
