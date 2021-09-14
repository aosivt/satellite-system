package org.satellite.system.web

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpHeader, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.settings.RoutingSettings
import org.apache.spark.sql.SparkSession
import org.satellite.system.core.Application
import spray.json.{JsString, JsValue}

import scala.collection.immutable.Seq

/**
  * This is where you define your XHR routes.
  *
  * @param application an Application
  */
class APIRoutes(application: Application, spark: SparkSession) {

  implicit val settings: RoutingSettings = RoutingSettings.apply(application.config)

  def routes: Route = {
    pathPrefix("api") {
      respondWithHeader(RawHeader("Access-Control-Allow-Origin", "*")) {
        Route.seal(concat(
          get {
            pathPrefix("get") {
              val df = spark.sqlContext.sql("select * from parquet.`/media/alex/058CFFE45C3C7827/maiskoe/result/*.parquet`")
              df.printSchema()
//              createDataset(spark.sparkContext.parallelize(template))(MyDataEncoders.myDataEncoder)
              complete(JsString(df.count().toString))
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

  object MyDataEncoders {
    implicit def myDataEncoder: org.apache.spark.sql.Encoder[String] =
      org.apache.spark.sql.Encoders.kryo[String]
  }

}
