package org.satellite.system

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import org.apache.spark.sql.SparkSession

import scala.io.StdIn
import org.satellite.system.core.Application
import org.satellite.system.web._
import org.satellite.system.http.{SPAWebServer, SocketWebServer}

import scala.concurrent.duration._

/**
  * The Main class that bootstraps the application.
  */
object Main extends App with SPAWebServer with SocketWebServer {

  implicit val app: Application = Application()
  implicit val spark: SparkSession = SparkSession.builder
    .master("local")
    .getOrCreate

  private val host = app.config.getString("http.host")
  private val port = app.config.getInt("http.port")
  private val stopOnReturn = app.config.getBoolean("http.stop-on-return")
  private val keepAliveInSec = app.config.getInt("http.webSocket.keep-alive")

  private val apiRoutes = new APIRoutes(app, spark)

  override implicit val system: ActorSystem = app.system
  override val socketActorProps: Props = SocketActor.props()
  override val keepAliveTimeout: FiniteDuration = keepAliveInSec.seconds
  override val routes: Route = apiRoutes.routes ~ super.routes

  start(host, port) foreach { _ =>
    if (stopOnReturn) {
      system.log.info(s"Press RETURN to stop...")
      StdIn.readLine()
      stop().onComplete(_ => app.shutdown())
    }
  }

}
