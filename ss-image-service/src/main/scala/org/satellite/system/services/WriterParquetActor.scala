package org.satellite.system.services

import akka.actor.{Actor, ActorSystem}
import org.apache.spark.sql.SparkSession

class WriterParquetActor()(implicit spark: SparkSession, system: ActorSystem) extends Actor with SparkSocketDtoSchema{
//  val ds = spark.read.parquet("/media/alex/058CFFE45C3C7827/ss/stg_data_satellite_images")
  def receive = {
    case jsons: Array[String] => write(jsons)
    case _ => println(s"WriterParquetActor")
  }
}
