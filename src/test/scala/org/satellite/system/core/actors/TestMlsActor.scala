package org.satellite.system.core.actors

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActors, TestKit, TestProbe}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.reflect.internal.MacroAnnotionTreeInfo
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
//      1 2 3 4 5 6
//      12 22 31 44 55 66
      val X = List(1D,2D,3D,4D,5D,6D)
      val Y = List(12D,22D,31D,44D,55D,66D)

      val SumXi = X.sum
      val SumYi = Y.sum
      val SqrSumXi = X.map(x=>Math.pow(x,2)).sum
      val SqrSumYi = Y.map(y=>Math.pow(y,2)).sum
      val XiYiSum = X.zip(Y).map((l: (Double, Double)) => l._1 * l._2).sum

      val A = ((SumXi * SumYi) - (X.length*XiYiSum))/(Math.pow(SumXi,2)-(X.length*SqrSumXi))
      val B = (SumXi*XiYiSum - (SqrSumXi*SumYi))/(Math.pow(SumXi,2)-(X.length*SqrSumXi))

      val Xsr = (1/X.length)*SumXi

      println(s"> SumXi : ${SumXi}")
      println(s"> SumYi : ${SumYi}")
      println(s"> SqrSumXi : ${SqrSumXi}")
      println(s"> SqrSumYi : ${SqrSumYi}")
      println(s"> XiYiSum : ${XiYiSum}")
      println(s"> A : ${A}")
      println(s"> B : ${B}")

      val Yreg = X.map(x=>A*x+B)

      val Rxy = ((X.length*XiYiSum) - (SumXi * SumYi)) / (Math.sqrt(((X.length*SqrSumXi)-Math.pow(SumXi,2))*((X.length*SqrSumYi)-Math.pow(SumYi,2))))

      val R2 = Math.pow(Rxy,2)

      val Ei = Y.zip(Yreg).map((l: (Double, Double)) => l._1 - l._2)
      val Ei2 = Ei.map(e=>Math.pow(e,2))
      val DeltaEi = (1 until X.length).map(ind=> Ei(ind)-Ei(ind-1))
      println(DeltaEi)

      val Ai = Y.zip(Yreg).map((l: (Double, Double))=>Math.abs((l._1-l._2)/l._1))

      val Aisr = (1D/X.length)*Ai.sum*100
    }
    "test sqr regression" in {
      //      1 2 3 4 5 6
      //      12 22 31 44 55 66
      val X = List(1D,2D,3D,4D,5D,6D)
      val Y = List(12D,22D,31D,44D,55D,66D)

      val SumXi = X.sum
      val SumYi = Y.sum
      val SqrSumXi = X.map(x=>Math.pow(x,2)).sum
      val SumXi3 = X.map(x=>Math.pow(x,3)).sum
      val SumXi4 = X.map(x=>Math.pow(x,4)).sum
      val SqrSumYi = Y.map(y=>Math.pow(y,2)).sum
      val XiYiSum = X.zip(Y).map((l: (Double, Double)) => l._1 * l._2).sum
      val Xi2YiSum = X.zip(Y).map((l: (Double, Double)) => Math.pow(l._1,2) * l._2).sum

      val A = ((SumXi * SumYi) - (X.length*XiYiSum))/(Math.pow(SumXi,2)-(X.length*SqrSumXi))
      val B = (SumXi*XiYiSum - (SqrSumXi*SumYi))/(Math.pow(SumXi,2)-(X.length*SqrSumXi))

      println(s"$SqrSumXi a + $SumXi b + ${X.length} c = $SumYi" )
      println(s"$SumXi3 a + $SqrSumXi b + $SumXi c = $XiYiSum" )
      println(s"$SumXi4 a + $SumXi3 b + $SqrSumXi c = $Xi2YiSum" )

      val M = Array.ofDim[Double](3,3)
      M(0) = Array(SqrSumXi,SumXi,X.length)
      M(1) = Array(SumXi3,SqrSumXi,SumXi)
      M(2) = Array(SumXi4,SumXi3,SqrSumXi)

      val Ma = Array.ofDim[Double](3,3)
      Ma(0) = Array(SumYi,SumXi,X.length)
      Ma(1) = Array(XiYiSum,SqrSumXi,SumXi)
      Ma(2) = Array(Xi2YiSum,SumXi3,SqrSumXi)

      val Mb = Array.ofDim[Double](3,3)
      Mb(0) = Array(SqrSumXi,SumYi,X.length)
      Mb(1) = Array(SumXi3,XiYiSum,SumXi)
      Mb(2) = Array(SumXi4,Xi2YiSum,SqrSumXi)

      val Mc = Array.ofDim[Double](3,3)
      Mc(0) = Array(SqrSumXi,SumXi,SumYi)
      Mc(1) = Array(SumXi3,SqrSumXi,XiYiSum)
      Mc(2) = Array(SumXi4,SumXi3,Xi2YiSum)


      def cramer : Array[Array[Double]]=> Double = m =>{
        (((m(0)(0) * m(1)(1) * m(2)(2)) + (m(0)(1) * m(1)(2) * m(2)(0)) + (m(0)(2) * m(1)(0) * m(2)(1))) - ((m(2)(0) * m(1)(1) * m(0)(2)) +(m(0)(1) * m(1)(0) * m(2)(2)) +(m(1)(2) * m(2)(1) * m(0)(0))))
      };

      val DM = cramer(M)
      val DMa = cramer(Ma)
      val DMb = cramer(Mb)
      val DMc = cramer(Mc)

      val a = DMa/DM
      val b = DMb/DM
      val c = DMc/DM

      val Yrg = X.map(x=>a*Math.pow(x,2) + b * x + c)
      Yrg.foreach(println)
      val Ysr = (1D/X.length)*SumYi
      val YsrY = Y.map(y=>y-Ysr)
      val YsrYSum = YsrY.sum
      val YsrY2 = YsrY.map(y=>Math.pow(y,2))
      val YsrYSum2 = YsrY2.sum
      val YrgY = Y.zip(Yrg).map(y=>y._1-y._2)
      val YrgY2 = YrgY.map(y=>Math.pow(y,2))
      val YrgY2Sum = YrgY2.sum
//            index correlation
      val R = Math.sqrt((1-(YrgY2Sum/YsrYSum2)))
//            index determination
      val R2 = Math.pow(R,2)

      val sumTest = (Y.zip(Yrg).map(y=>Math.abs(y._1-y._2)/y._1))
//      Average approximation error
      val Asr = ((1D/X.length)*sumTest.sum) * 100

      println(R)
      println(R2)
      println(Asr)
    }

  }
}
