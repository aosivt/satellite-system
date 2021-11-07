package org.satellite.system.services

import org.apache.spark.internal.Logging
import org.apache.spark.network.util.JavaUtils
import org.apache.spark.sql.streaming.DataStreamReader
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}
import org.apache.spark.sql.functions.{explode, split}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.receiver.Receiver
import org.satellite.system.core.Application

import java.io.{BufferedInputStream, BufferedOutputStream, BufferedReader, DataOutputStream, IOException, InputStream, InputStreamReader, OutputStream, PrintStream, PrintWriter}
import java.net.{ServerSocket, Socket, SocketException, SocketImpl, SocksSocketImpl}
import java.nio.charset.StandardCharsets

//object SparkSocket{
//  def apply(spark: SparkSession, app: Application): SparkSocket = new SparkSocket(spark, app)
//}


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

class SparkSocket()(implicit spark: SparkSession) extends Serializable {

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
  private def initListener(): Unit ={
    val listener = new ServerSocket(9999)
    new Thread(){
      setDaemon(true)
      override def run(): Unit = {
        while (true) {
          val server = listener.accept
          val output = server.getOutputStream
          val writer = new PrintWriter(output, true)
          writer.println("This is a message sent to ther server")
        }
      }
    }.start();


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
