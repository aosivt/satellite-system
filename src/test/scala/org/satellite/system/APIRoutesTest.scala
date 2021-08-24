package org.satellite.system

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, MediaTypes, MessageEntity, StatusCodes}

import scala.concurrent.duration._
import scala.util.{Failure, Success}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestProbe
import akka.util.{ByteString, Timeout}
import org.satellite.system.core.Application
import org.satellite.system.web.APIRoutes
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import spray.json.DefaultJsonProtocol._
import spray.json.{JsString, RootJsonFormat, jsonWriter}

case class User(name: String, age: Int)
object User{
  implicit val userJsonFormat: RootJsonFormat[User] = jsonFormat2(User.apply)
}

class APIRoutesTest extends AnyWordSpec with Matchers with ScalaFutures with ScalatestRouteTest{
//  extends AnyWordSpec with Matchers with ScalatestRouteTest {

  val request = HttpRequest(uri = "/api/get")

  val postRequest = HttpRequest(uri = "/api")



  implicit val app: Application = Application()
  // the Akka HTTP route testkit does not yet support a typed actor system (https://github.com/akka/akka-http/issues/2036)
  // so we have to adapt for now
  lazy val testKit = ActorTestKit()
  implicit def typedSystem = testKit.system
  override def createActorSystem(): akka.actor.ActorSystem = testKit.system.classicSystem
  // Here we need to implement all the abstract members of UserRoutes.
  // We use the real UserRegistryActor to test it while we hit the Routes,
  // but we could "mock" it by implementing it in-place or by using a TestProbe
  // created with testKit.createTestProbe()
  val apiRoutes = new APIRoutes(app)
  lazy val routes = apiRoutes.routes
  val user = User("Kapi", 42)

  "The service" should {
    "Test API" in {
      request ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===("\"OK GET\"")
      }
    }

    "Test API inside" in {
      val jsonRequest = ByteString(
        s"""
           |{
           |    "name":"test"
           |}
        """.stripMargin)
      val postRequest = HttpRequest(
        HttpMethods.POST,
        uri = "/api",
        entity = HttpEntity(MediaTypes.`application/json`, jsonRequest))
      val putRequest = HttpRequest(
        HttpMethods.PUT,
        uri = "/api",
        entity = HttpEntity(MediaTypes.`application/json`, jsonRequest))

      // tests:
      Get("/api/get") ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        responseAs[String] shouldEqual ("\"OK GET\"")
      }
      postRequest ~> routes ~> check {
        status should ===(StatusCodes.OK)

        contentType should ===(ContentTypes.`application/json`)
        responseAs[String] shouldEqual ("\"OK POST\"")
      }
      putRequest ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        responseAs[String] shouldEqual ("\"OK PUT\"")
      }
      Delete("/api/") ~> routes ~> check {
        status should ===(StatusCodes.OK)
      }

    }
  }
}