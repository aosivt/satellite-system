package org.satellite.system.web

import akka.actor.ActorRef
import akka.actor.TypedActor.context
import akka.event.Logging
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpHeader, StatusCodes}
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives.{as, complete, concat, delete, entity, get, onComplete, options, pathPrefix, post, put, respondWithHeader, respondWithHeaders}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.settings.RoutingSettings
import org.apache.spark.sql.SparkSession
import org.satellite.system.Main.db
import org.satellite.system.Main.materializer.executionContext
import org.satellite.system.core.Application
import org.satellite.system.core.db.SatelliteSystemPgProfile
import org.satellite.system.core.db.table.{Bank, articles, banks}
import spray.json.{DefaultJsonProtocol, JsString, JsValue, RootJsonFormat}

import scala.collection.immutable.Seq
import scala.concurrent.Future
import org.satellite.system.core.db.SatelliteSystemPgProfile.api._

trait BankJsonSupport extends SprayJsonSupport with DefaultJsonProtocol{
  implicit val bankFormat: RootJsonFormat[Bank] = jsonFormat6(Bank) // contains List[Item]
}

class BankRouters(application: Application,db: SatelliteSystemPgProfile.backend.DatabaseDef) extends BankJsonSupport{
  implicit val settings: RoutingSettings = RoutingSettings.apply(application.config)

  def getAll(): Future[Array[Bank]] =
    db.run(banks.result.map(r=>r.map(row=>Bank(row.id,row.bankbik,row.koraccount,row.name,row.inn,row.bankState)).toArray))

  def routes: Route = {
    pathPrefix("bank") {
      respondWithHeader(RawHeader("Access-Control-Allow-Origin", "*")) {
        Route.seal(concat(
          get {
            onComplete(getAll()){
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
