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
import spray.json.{DefaultJsonProtocol, JsString, JsValue, RootJsonFormat, jsonWriter}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport

import scala.collection.immutable.Seq
import org.satellite.system.Main.materializer.executionContext

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

    val ds1 = spark.sqlContext.sql(
      "select rowId, projection, geoTransform, collect_list(data) result " +
        " from (" +
        " select " +
        " (case when indArray < 3 then rowId - 1 when 3 <= indArray and indArray < 6 then rowId when 6 <= indArray  then rowId + 1 end) rowId, " +
        " (case when array_contains(Array(0,3,6),indArray) then colId - 1 when array_contains(Array(2,5,8),indArray) then colId + 1 else colId end)  colId, " +
        " width, height, projection, geoTransform, data, projection" +
        " from parquet.`/media/alex/058CFFE45C3C7827/ss/stg_data_satellite_images/name=S2A_MSIL1C_20170308T051641_N0204_R062_T45UVA_20170308T051723/*.parquet`" +
        " lateral view posexplode(dataRed) results AS indArray, data " +
        " where rowId%10=0 and colId%10=0 " +
        " order by rowId, colId" +
        " ) " +
        " group by rowId, projection, geoTransform" +
        " ")
      ds1.collect().map(m=>Order(m.getInt(0),m.getString(1).replace("'","\""),m.getSeq[Double](2),m.getSeq[Double](3)))
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

}
