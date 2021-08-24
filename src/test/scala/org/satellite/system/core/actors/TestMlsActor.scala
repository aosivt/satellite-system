package org.satellite.system.core.actors

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActors, TestKit, TestProbe}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration._
import scala.language.postfixOps
class TestMlsActor extends TestKit(ActorSystem("MySpec"))
  with ImplicitSender
  with AnyWordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "An Echo actor" must {

    "send back messages unchanged" in {
      val echo = system.actorOf(TestActors.echoActorProps)
      echo ! "hello world"
      expectMsg("hello world")
    }
    "send mls actor" in {
      val probe1 = TestProbe()
      val probe2 = TestProbe()
      val actor = system.actorOf(Props[MlsActor]())
      actor ! ((probe1.ref,probe2.ref))
      actor ! "template"
      probe1.expectMsg(500 millis, "template")
      probe2.expectMsg(500 millis, "template")
    }
    "test line regression" in {
      val X = List(1D,2D,3D,4D,5D,6D)
      val Y = List(12D,22D,31D,44D,55D,66D)
      val SumXi = X.sum
      val SumYi = Y.sum
      val SqrSumXi = X.map(x=>Math.pow(x,2)).sum
      val SqrSumYi = Y.map(y=>Math.pow(y,2)).sum
      val XiYiSum = X.zip(Y).map((l: (Double, Double)) => l._1 * l._2).sum

      val A = ((SumXi * SumYi) - (X.length*XiYiSum))/(Math.pow(SumXi,2)-(X.length*SqrSumXi))
      val B = (SumXi*XiYiSum - (SqrSumXi*SumYi))/(Math.pow(SumXi,2)-(X.length*SqrSumXi))

      println(s"> SumXi : ${SumXi}")
      println(s"> SumYi : ${SumYi}")
      println(s"> SqrSumXi : ${SqrSumXi}")
      println(s"> SqrSumYi : ${SqrSumYi}")
      println(s"> XiYiSum : ${XiYiSum}")
      println(s"> A : ${A}")
      println(s"> B : ${B}")

      val Yreg = X.map(x=>A*x-B)

      Yreg.foreach(println)

    }


  }
}
