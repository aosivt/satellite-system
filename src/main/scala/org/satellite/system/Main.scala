package org.satellite.system

import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import org.apache.spark.sql.SparkSession

import scala.io.StdIn
import org.satellite.system.core.Application
import org.satellite.system.core.db.SatelliteSystemDataBase
import org.satellite.system.web._
import org.satellite.system.http.{SPAWebServer, SocketWebServer}
import org.satellite.system.services.{SocketConnector}

import scala.concurrent.duration._
/**
  * The Main class that bootstraps the application.
  */
object Main extends App with SPAWebServer with SocketWebServer with SocketConnector {

  implicit val app: Application = Application()
  implicit val spark: SparkSession = SparkSession.builder
    .master("local")
    .config("spark.driver.maxResultSize","4g")
    .getOrCreate

  private val host = app.config.getString("http.host")
  private val port = app.config.getInt("http.port")
  private val stopOnReturn = app.config.getBoolean("http.stop-on-return")
  private val keepAliveInSec = app.config.getInt("http.webSocket.keep-alive")


  val db = SatelliteSystemDataBase.apply()
  override implicit val system: ActorSystem = app.system
  override val socketActorProps: Props = SocketActor.props()
  override val keepAliveTimeout: FiniteDuration = keepAliveInSec.seconds

  private val apiRoutes = new APIRoutes(app, spark, usersSocket)
  private val apiSatelliteImageRoutes = new SatelliteImageRouters(app)

  override val routes: Route = apiRoutes.routes ~ apiSatelliteImageRoutes.routes ~ super.routes

  initSocket()

  start(host, port) foreach { _ =>
    if (stopOnReturn) {
      system.log.info(s"Press RETURN to stop...")
      StdIn.readLine()
      stop().onComplete(_ => app.shutdown())
    }
  }

}
