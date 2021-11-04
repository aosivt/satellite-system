package org.satellite.system.web

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpHeader, StatusCodes}
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives.{as, complete, concat, delete, entity, get, onComplete, options, pathPrefix, post, put, respondWithHeader, respondWithHeaders}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.settings.RoutingSettings
import org.satellite.system.core.Application
import org.satellite.system.core.db.DBComponent
import org.satellite.system.core.db.table.{File, SatelliteImage, SatelliteImageRepository}
import spray.json.{DefaultJsonProtocol, JsString, JsValue, RootJsonFormat}

import scala.collection.immutable.Seq

trait SatelliteImageJsonSupport extends SprayJsonSupport with DefaultJsonProtocol{
  implicit val satelliteImageFormat: RootJsonFormat[SatelliteImage] = jsonFormat6(SatelliteImage) // contains List[Item]
  implicit val fileFormat: RootJsonFormat[File] = jsonFormat2(File) // contains List[Item]
}


class SatelliteImageRouters(application: Application) extends SatelliteImageJsonSupport with SatelliteImageRepository with DBComponent{

  implicit val settings: RoutingSettings = RoutingSettings.apply(application.config)
  def routes: Route = {
    pathPrefix("SatelliteImage") {
      respondWithHeader(RawHeader("Access-Control-Allow-Origin", "*")) {
        Route.seal(concat(
          get {
            onComplete(getAllSatelliteImageWithFile){
              result => complete(result)
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
//          delete {
//            complete(StatusCodes.OK)
//          },
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
