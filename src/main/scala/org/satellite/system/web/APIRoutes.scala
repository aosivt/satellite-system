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
      "select rowId, projection, geoTransform, result " +
        " from parquet.`/media/alex/058CFFE45C3C7827/maiskoe/2016/лето/parquet/ndvi/S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE/*.parquet`")
    ds1.show(2)
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
