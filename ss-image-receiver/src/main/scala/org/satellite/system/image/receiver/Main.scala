package org.satellite.system.image.receiver

import org.apache.spark.sql.SparkSession

object Main extends App{
  implicit val spark: SparkSession = SparkSession.builder
    .master("local[2]")
    .config("spark.driver.maxResultSize","4g")
    .getOrCreate

  def test(): Unit ={
    try{
      val df = spark.readStream
        .format("socket")
        .option("host","localhost")
        .option("port","9999")
        .load()
      val wordsDF = df.select(df("value")).alias("word")
      wordsDF.printSchema()
      wordsDF.groupBy("value").count().writeStream
        .format("console")
        .outputMode("complete")
        .start()
        .awaitTermination(1000)
    }catch {
      case e:Exception =>
        println("Could not listen on port: 9999.")
        Thread.sleep(3000)
        test()
    }
  }
  test()
}