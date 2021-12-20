package org.satellite.system.services

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.io.Tcp.{Bind, Bound, CommandFailed, Connected, Register}
import akka.io.{IO, Tcp}
import akka.util.ByteString
import io.netty.handler.codec.serialization.ObjectDecoderInputStream
import org.apache.avro.io.Encoder
import org.apache.spark.internal.Logging
import org.apache.spark.network.util.JavaUtils
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder
import org.apache.spark.sql.streaming.{DataStreamReader, DataStreamWriter, OutputMode}
import org.apache.spark.sql.{DataFrame, Dataset, Encoders, Row, SaveMode, SparkSession}
import org.apache.spark.sql.functions.{explode, split}
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.receiver.Receiver
import org.satellite.system.Main
import org.satellite.system.Main.system
import org.satellite.system.core.Application
import org.satellite.system.image.converter.core.DtoSparkImagePart

import java.io.{BufferedInputStream, BufferedOutputStream, BufferedReader, ByteArrayOutputStream, DataOutputStream, IOException, InputStream, InputStreamReader, ObjectInputStream, ObjectOutputStream, OutputStream, PrintStream, PrintWriter}
import java.net.{InetSocketAddress, ServerSocket, Socket, SocketException, SocketImpl, SocksSocketImpl}
import java.nio.charset.StandardCharsets
import java.util.Date
import java.util.concurrent.{ExecutorService, Executors}
import java.util.stream.IntStream
import scala.annotation.tailrec
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import scala.util.Try

//object SparkSocket{
//  def apply(spark: SparkSession, app: Application): SparkSocket = new SparkSocket(spark, app)
//}

object SimplisticHandler{
  def props(sparkSender :Option[ActorRef])(implicit spark: SparkSession) = Props(new SimplisticHandler(sparkSender :Option[ActorRef]))
}

class SimplisticHandler(sparkSender :Option[ActorRef])(implicit spark: SparkSession) extends Actor with SparkSocketDtoSchema with ActorLogging {
  import Tcp._
  var listDto = new scala.collection.mutable.ListBuffer[DtoSparkImagePart]()
  val ssc = new StreamingContext(spark.sparkContext, Seconds(1))

  val queue = ArrayBuffer[DtoSparkImagePart]()
  val bufferQueue = ArrayBuffer[DtoSparkImagePart]()

  val writer: () => Unit = ()=> {
    if(queue.nonEmpty){
      val seq = ArrayBuffer[DtoSparkImagePart]()
      queue.copyToBuffer(seq)
      queue.clear()
      val head = seq.head
//      val path = head.getPlacePath
      val df = toDF(seq)
//      df.coalesce(1).write.mode(SaveMode.Append).parquet(path)
    }
  }

  val sendToQueue: DtoSparkImagePart => Unit = (set:DtoSparkImagePart) => {
        if(queue.size > 200000) {
          bufferQueue += set
          writer()
        } else {
          if (bufferQueue.nonEmpty){
            bufferQueue.copyToBuffer(queue)
            bufferQueue.clear()
          }
          queue += set
        }
  }

  def receive: Receive = {

    case Received(data) => {

      if (stored > 1000000){
        val temp = this.synchronized(buffer(data))
        if (temp.nonEmpty){
          //        val ds = jsonConvert(temp)
          writerParquet ! temp
          //        sparkSender.get ! Write(ByteString.apply(temp(0)))
        }
      } else{
        buffer(data)
      }


    }
//    case NoAck(ack)=>
//    {
//      try {
//            val bytes = data.toArray;
//            sendToQueue(deserialize(bytes))
//      }catch {
//        case e:Exception => print(e)
//      }
//
//    }

    case PeerClosed => context.stop(self)
  }
  import java.io.ByteArrayInputStream
  import java.io.IOException
  import java.io.ObjectInputStream

  @throws[IOException]
  @throws[ClassNotFoundException]
  def deserialize(data: Array[Byte]): DtoSparkImagePart = {
    val in = new ByteArrayInputStream(data)
    val is = new ObjectInputStream(in)
    val value = is.readObject.asInstanceOf[DtoSparkImagePart]
    is.close()
    value
  }
  def serialise(value: Any): Array[Byte] = {
    val stream: ByteArrayOutputStream = new ByteArrayOutputStream()
    val oos = new ObjectOutputStream(stream)
    oos.writeObject(value)
    oos.close()
    stream.toByteArray
  }
  private var storage = Vector.empty[ByteString]
  private var headStorage = Vector.empty[ByteString]
  @volatile private var stored = 0L
  val writerParquet = Main.system.actorOf(Props(new WriterParquetActor()))
//  private val ds = spark.createDataFrame(Seq.empty).
//
//    writeStream.format("parquet")
//    .partitionBy("name")
//    .outputMode(OutputMode.Append)
//    .start("/media/alex/058CFFE45C3C7827/ss/stg_data_satellite_images").awaitTermination()

  private def buffer(data: ByteString): Array[String] = {
    storage :+= data
    stored += data.size
    if (stored > 10000000){
      stored = 0L
      headStorage ++= storage.take(storage.size)
      storage = storage.drop(storage.size)

      val temp = headStorage.map(s=>s.utf8String).reduce((s1,s2)=> s1.concat(s2)).split("\n")
      val resultArray = temp.take(temp.length-2)
//      println(resultArray(0))
      headStorage = headStorage.drop(headStorage.length)
      headStorage :+= ByteString.apply(temp(temp.length-1))
      return resultArray
//      writerParquet ! resultArray



//      streamDs.jo
//      new Thread(){
//        override def run(): Unit = {
//          val ds = jsonConvert(resultArray)
//          ds.coalesce(1).write.mode(SaveMode.Overwrite)
//            .partitionBy("name")
//            .parquet("/media/alex/058CFFE45C3C7827/ss/stg_data_satellite_images")
//        }
//      }.start()
    }
    Array.empty[String]

//    if (stored>2200){
//      var tryDto = Vector.empty[ByteString]
//      val countStorage = storage.length;
//      val head = storage(0);
//        IntStream.rangeClosed(1,countStorage).forEach(f=>{
//            try{
//              tryDto ++= storage.dropRight(storage.length-1 - f).drop(f)
//              val dto = deserialize((head++tryDto.flatten).toArray);
//              val tail = storage.drop(f+1)
//              sendToQueue(dto)
//              storage = storage.drop(storage.length)
//              storage :+= head
//              storage = storage ++ tail
//              tryDto = Vector.empty[ByteString]
//              stored = storage.flatten.size
//            } catch {
//              case e: Exception => println(e)
//            }
//        })


      //      val selfByteObject = storage.slice(1, storage.size).flatten
//      val dto = deserialize((storage(0) ++ selfByteObject).toArray);
//      val tail = selfByteObject.slice(serialise(dto).length ,selfByteObject.size)
//      sendToQueue(dto)
//      storage = storage.slice(0, 1)
//      stored = 4 + tail.length
//    }
  }
}


object TCPServer{
  def props()(implicit spark: SparkSession) = Props(new TCPServer())
}
class TCPServer(implicit spark: SparkSession) extends Actor {

  import akka.stream.javadsl.Tcp._
  import context.system

  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", 9999))

  var sparkSender : Option[ActorRef] = Option.empty;

  def receive: Receive = {
    case b @ Bound(_) =>
      context.parent ! b

    case CommandFailed(_: Bind) => context.stop(self)

    case c @ Connected(remote, local) =>
      val connection = sender()
      if (sparkSender.isEmpty){
        sparkSender = Option.apply(connection)

      }
      val handler = context.actorOf(SimplisticHandler.props(sparkSender))

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

class SparkSocket()(implicit spark: SparkSession, system: ActorSystem) extends Serializable with SparkSocketDtoSchema {
//  val encoder: ExpressionEncoder[DtoSparkImagePart] = ExpressionEncoder()
type MyObjEncoded = (Int, Int)
implicit val encoder = Encoders.javaSerialization[DtoSparkImagePart]
private val writerParquet = Executors.newFixedThreadPool(2)
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
//    initListener()
    initServer()
//    initSpark()

  }
  private def initSpark(): Unit ={
    new Thread(){
      setDaemon(true)
      override def run(): Unit = {
        val df = getDataStreamReader.load();
        val wordsDF = df.select(df("value")).alias("word")
        wordsDF.printSchema()
        wordsDF.groupBy("value").count().writeStream
          .format("console")
          .outputMode("complete")
          .start()
          .awaitTermination()
//        wordsDF.show()
//        val count = wordsDF.groupBy("word").count()
//        query(wordsDF)
      }
    }.start()
  }
  def createSchema[T <: Product]()(implicit tag: scala.reflect.runtime.universe.TypeTag[T]) = Encoders.product[T].schema
  private def initListener(): Unit ={
    implicit val context = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())
    var listDto = new scala.collection.mutable.ListBuffer[DtoSparkImagePart]()
    val ssc = new StreamingContext(spark.sparkContext, Seconds(1))

    val queue = ArrayBuffer[DtoSparkImagePart]()
    val bufferQueue = ArrayBuffer[DtoSparkImagePart]()

    val writer = ()=> {
      if(queue.nonEmpty){
        val seq = ArrayBuffer[DtoSparkImagePart]()
        queue.copyToBuffer(seq)
        queue.clear()
        val head = seq.head
//        val path = head.getPlacePath
        val df = toDF(seq)
//        df.write.mode(SaveMode.Append).parquet(path)
        printf("Дата вхождения writer: %s ||| Номер строки: %s \n", new Date(), head.getRowId)
      }
    }

    val sendToQueue = (set:DtoSparkImagePart) => {
         if(queue.size > 200000 || set.getRowId == -1) {
           bufferQueue += set
           writer()
         } else {
           if (bufferQueue.nonEmpty){
            bufferQueue.copyToBuffer(queue)
            bufferQueue.clear()
           }
           queue += set
//            val path = set.head.getPlacePath
//            val df = toDF(set)
//            df.write.mode(SaveMode.Append).parquet(path)
//            printf("Дата вхождения: %s ||| Номер строки: %s \n", new Date(), set.head.getRowId)
         }
    }

    val listener = new ServerSocket(9999)
    new Thread(){
      setDaemon(true)
      override def run(): Unit = {
        while (true) {
          val server = listener.accept
          import java.io.ObjectInputStream
          def getDto:() => Unit = () => {
              val inputStream = server.getInputStream
              val objectInputStream = new ObjectInputStream(inputStream)
            try{
              val dto = objectInputStream.readObject.asInstanceOf[DtoSparkImagePart]
              if (dto.getRowId != -1){
                sendToQueue(dto)
                getDto()
              } else {
                inputStream.close()
                objectInputStream.close()
                server.close()
              }
            } catch {
              case e:Exception =>
                println("Could not listen on port: 9999.")
                Thread.sleep(3000)
                getDto()
            }
            }
            getDto()
        }
      }
    }.start();
  }

//  private def write(set: Seq[DtoSparkImagePart]):Unit = {
//    val path = set.head.getPlacePath
//    val df = toDF(set)
//    df.write.mode(SaveMode.Append).parquet(path)
//  }


  private def initServer(): Unit = {
    system.actorOf(TCPServer.props())
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

  }
  class CustomReceiver(ous: InputStream) extends Receiver[Seq[DtoSparkImagePart]](StorageLevel.MEMORY_AND_DISK_2) with Logging {
    def onStart(): Unit = {
      // Start the thread that receives data over a connection
      new Thread("Socket Receiver") {
        override def run() {
          try {
            val inputStream = ous
            // create a DataInputStream so we can read data from it.
            val objectInputStream = new ObjectInputStream(inputStream)

            val dto = objectInputStream.readObject.asInstanceOf[Array[DtoSparkImagePart]]

            // Until stopped or connection broken continue reading
            val reader = new BufferedReader(
              new InputStreamReader(ous, StandardCharsets.UTF_8))

//            var userInput = reader.readLine()
//            while(!isStopped && userInput != null) {
//              store(userInput)
//              userInput = reader.readLine()
//            }
//            reader.close()
//            socket.close()

            // Restart in an attempt to connect again when server is active again
//            restart("Trying to connect again")
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
