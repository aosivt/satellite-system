package org.satellite.system.web

import akka.actor.{ActorRef, Props}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpHeader, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.settings.RoutingSettings
import org.apache.spark.sql.SparkSession
import org.codehaus.jackson.map.ObjectMapper
import org.json4s.jackson.Json
import org.satellite.system.core.Application
import spray.json.{JsString, JsValue, JsonParser}

import scala.collection.immutable.Seq
import org.apache.spark.sql.Encoders
/**
  * This is where you define your XHR routes.
  *
  * @param application an Application
  */
class APIRoutes(application: Application, spark: SparkSession, usersSocket: Array[ActorRef]) {

  implicit val settings: RoutingSettings = RoutingSettings.apply(application.config)
  private val encoder = Encoders.bean(classOf[PlotlyData])

  def routes: Route = {
    pathPrefix("api") {
      respondWithHeader(RawHeader("Access-Control-Allow-Origin", "*")) {
        Route.seal(concat(
          get {
            pathPrefix("get") {

              val df = spark.sqlContext.sql("select rowId,S2A_MSIL1C_20160611T051652_N0202_R062_T45UVA_20160611T051654_SAFE as result from parquet.`/media/alex/058CFFE45C3C7827/maiskoe/result/*.parquet`")
              df.collect().foreach(r=>{
                usersSocket(0) ! JsonParser(r.json)
              })

              complete(StatusCodes.OK)
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
