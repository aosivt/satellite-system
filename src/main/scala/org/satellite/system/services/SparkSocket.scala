package org.satellite.system.services

import org.apache.spark.sql.streaming.DataStreamReader
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}
import org.apache.spark.sql.functions.{explode, split}
import org.satellite.system.Main.app
import org.satellite.system.core.Application

trait SparkSocket {
  implicit val spark: SparkSession
  implicit val app: Application

  /**
   *
   */
  protected def initSocket(): Unit = {
    val df = getDataStreamReader.load();
    val wordsDF = df.select(explode(split(df("value")," ")).alias("word"))
    val count = wordsDF.groupBy("word").count()
    val query = count.writeStream
      .format("console")
      .outputMode("complete")
      .start()
      .awaitTermination()
  }

  /**
   * @return
   */
  private def getDataStreamReader: DataStreamReader = {
    spark.readStream
        .format("socket")

        .option("host",app.config.getString("spark.listenSocket.host"))
        .option("port",app.config.getString("spark.listenSocket.port"))
  }
}
