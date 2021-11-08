package org.satellite.system.services

import akka.actor.{Actor, ActorSystem, Props}
import akka.io.Tcp.{Bind, Bound, CommandFailed, Connected, Register}
import akka.io.{IO, Tcp}
import org.apache.avro.io.Encoder
import org.apache.spark.internal.Logging
import org.apache.spark.network.util.JavaUtils
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder
import org.apache.spark.sql.streaming.DataStreamReader
import org.apache.spark.sql.{DataFrame, Dataset, Encoders, Row, SparkSession}
import org.apache.spark.sql.functions.{explode, split}
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.receiver.Receiver
import org.satellite.system.core.Application
import org.satellite.system.image.converter.core.DtoSparkImagePart

import java.io.{BufferedInputStream, BufferedOutputStream, BufferedReader, DataOutputStream, IOException, InputStream, InputStreamReader, OutputStream, PrintStream, PrintWriter}
import java.net.{InetSocketAddress, ServerSocket, Socket, SocketException, SocketImpl, SocksSocketImpl}
import java.nio.charset.StandardCharsets
import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import scala.util.Try

//object SparkSocket{
//  def apply(spark: SparkSession, app: Application): SparkSocket = new SparkSocket(spark, app)
//}

class SimplisticHandler extends Actor {
  import Tcp._
  def receive = {
    case Received(data) => {
      try {
        val t = deserialize(data.toArray).asInstanceOf[DtoSparkImagePart]
        if (t.getRowId==0){
          context.stop(self)
        }
        print(t.getRowId)

      }catch {
        case e:Exception => print(e)
      }

    }

    case PeerClosed     =>
//      context.stop(self)
  }
  import java.io.ByteArrayInputStream
  import java.io.IOException
  import java.io.ObjectInputStream

  @throws[IOException]
  @throws[ClassNotFoundException]
  def deserialize(data: Array[Byte]): Any = {
    val in = new ByteArrayInputStream(data)
    val is = new ObjectInputStream(in)
    is.readObject
  }
}

class TCPServer extends Actor {

  import akka.stream.javadsl.Tcp._
  import context.system

  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", 9999))

  def receive: Receive = {
    case b @ Bound(_) =>
      context.parent ! b

    case CommandFailed(_: Bind) => context.stop(self)

    case c @ Connected(remote, local) =>
      val handler = context.actorOf(Props[SimplisticHandler]())
      val connection = sender()
      connection ! Register(handler)

  }
}


case class SparkServerSocket(port: Int) extends ServerSocket(port) with Serializable{
  override def accept(): SparkClientSocket = {
    if (isClosed) throw new SocketException("Socket is closed")
    if (!isBound) throw new SocketException("Socket is not bound yet")
    val s = SparkClientSocket(null.asInstanceOf[SocketImpl])
    implAccept(s)
    s
  }
}
case class SparkClientSocket(socket: SocketImpl) extends Socket(socket) with Serializable{
  //  override def getInputStream(): InputStream = {
  //    var is: InputStream = null
  //    try is = AccessController.doPrivileged(new PrivilegedExceptionAction[InputStream]() {
  //      @throws[IOException]
  //      override def run: InputStream = {
  //        impl.getInputStream
  //      }
  //    })
  //    catch {
  //      case e: PrivilegedActionException =>
  //        throw e.getException.asInstanceOf[IOException]
  //    }
  //    is
  //  }
//  override def isConnected: Boolean = true
//  override def isClosed: Boolean = false
}

class SparkSocket()(implicit spark: SparkSession, system: ActorSystem) extends Serializable {
import spark.implicits._
//  val encoder: ExpressionEncoder[DtoSparkImagePart] = ExpressionEncoder()
type MyObjEncoded = (Int, Int)
implicit val encoder = Encoders.javaSerialization[DtoSparkImagePart]

  // implicit conversions
implicit def toEncoded(o: DtoSparkImagePart): MyObjEncoded = (o.getRowId,o.getColId)
implicit def fromEncoded(e: MyObjEncoded): DtoSparkImagePart = {
  val template = new DtoSparkImagePart()
  template.setRowId(e._1)
  template.setColId(e._1)
  template
}
  //  implicit val spark: SparkSession
//  implicit val app: Application
class SparkSocketTread()(count: DataFrame) extends Thread{
  override def run(): Unit = {
    try {
      count.writeStream
        .format("console")
        .outputMode("complete")
        .start()
        .awaitTermination()
    } catch {
      case e:Exception =>{
        println("Could not listen on port: 9999.")
        Thread.sleep(3000)
        query(count)
      }
    }
  }
}

  /**
   *
   */
  def initSocket(): Unit = {
    initListener()
//    initSpark()
//    initServer()
  }
  private def initSpark(): Unit ={
    new Thread(){
      setDaemon(true)
      override def run(): Unit = {
        val df = getDataStreamReader.load();
        val wordsDF = df.select(explode(split(df("value"), " ")).alias("word"))
        val count = wordsDF.groupBy("word").count()
        query(count)
      }
    }.start()
  }
  def createSchema[T <: Product]()(implicit tag: scala.reflect.runtime.universe.TypeTag[T]) = Encoders.product[T].schema
  private def initListener(): Unit ={
    val schema = StructType(Array(StructField(name = "rowId", dataType = IntegerType, nullable = false)))
    val listener = new ServerSocket(9999)
    new Thread(){
      setDaemon(true)
      override def run(): Unit = {
//        val en = Encoders.javaSerialization()
        while (true) {
          val server = listener.accept
          import java.io.ObjectInputStream
          // get the input stream from the connected socket
          val inputStream = server.getInputStream
          // create a DataInputStream so we can read data from it.
          val objectInputStream = new ObjectInputStream(inputStream)

          def getDto:() => Unit = () => {
            val dto = objectInputStream.readObject.asInstanceOf[DtoSparkImagePart]
            if (dto.getRowId!=0){
              print(dto.getRowId)

              val temp = Seq(Row(dto.getRowId));
              val t = spark.sparkContext.parallelize(temp)
              val df = spark.createDataFrame(t,schema)
              df.show()
              df.printSchema()
              getDto()
            }
          }
          getDto()
          inputStream.close()
          objectInputStream.close()
          server.close()
        }
      }
    }.start();
  }


  private def initServer(): Unit = {
    system.actorOf(Props[TCPServer])
//    val s = new TCPServer
//    s.preStart();
  }

//    val ssc = new StreamingContext(spark.sparkContext, Seconds(1))
//    ssc.start()
//    ssc.awaitTermination()
//    try{
//      println("Waiting for connection")
//      val listener = new ServerSocket(19999);
//      new Thread("SparkServerSocket") {
//        setDaemon(true)
//        override def run(): Unit = {
//          var sock: Socket = null
//          try {
//                      logTrace(s"Waiting for connection on port ${serverSocket.getLocalPort}")
//
//            while(true) {
//              sock = listener.accept()
//
//              val is = new BufferedInputStream(sock.getInputStream)
//                          val os = new PrintStream(new BufferedOutputStream(sock.getOutputStream))//new
//                          val out = new DataOutputStream(sock.getOutputStream);
//              while (is.available() < 1) {
//                Thread.sleep(30)
//              }
//              val buf = new Array[Byte](is.available)
//              is.read(buf)
//              println(new String(buf))
//                          out.writeBytes(new String("Hello PHP"))
//                          out.close()
//              val lines = ssc.receiverStream(new CustomReceiver(sock.getInputStream))
//              val words = lines.flatMap(_.split(" "))
//              words.print()
//                          logTrace(s"Connection accepted from address ${sock.getRemoteSocketAddress}")
//                          authHelper.authClient(sock)
//                          logTrace("Client authenticated")
//                          promise.complete(Try(handleConnection(sock)))
//              is.close()
//              sock.close()

              //            lines.print()

//            }
//          } finally {
//            logTrace("Closing server")
//            JavaUtils.closeQuietly(listener)
//            JavaUtils.closeQuietly(sock)
//          }
//        }
//      }.start()

//    }
//    catch
//    {
//      case e:SocketException =>
//        println("Could not listen on port: 9898.")
//    }
//  }
  def query(count:DataFrame): Unit ={
    new SparkSocketTread()(count).run()
  }
  /**
   * @return
   */
  private def getDataStreamReader: DataStreamReader = {
    spark.readStream
        .format("socket")
        .option("host","localhost")
        .option("port","9999")
//        .option("host",app.config.getString("spark.listenSocket.host"))
//        .option("port",app.config.getString("spark.listenSocket.port"))
  }
  class CustomReceiver(ous: InputStream)
    extends Receiver[String](StorageLevel.MEMORY_AND_DISK_2) with Logging {



    def onStart(): Unit = {

      // Start the thread that receives data over a connection
      new Thread("Socket Receiver") {
        override def run() {
          try {
            // Until stopped or connection broken continue reading
            val reader = new BufferedReader(
              new InputStreamReader(ous, StandardCharsets.UTF_8))
            var userInput = reader.readLine()
            while(!isStopped && userInput != null) {
              store(userInput)
              userInput = reader.readLine()
            }
            reader.close()
//            socket.close()

            // Restart in an attempt to connect again when server is active again
            restart("Trying to connect again")
          }
          catch {
            case e: SocketException =>
              e.printStackTrace();
          }
        }
      }.start()
    }

    def onStop(): Unit = {
      // There is nothing much to do as the thread calling receive()
      // is designed to stop by itself isStopped() returns false
    }
  }

}
