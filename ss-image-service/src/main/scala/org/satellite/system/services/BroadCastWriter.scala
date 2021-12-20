package org.satellite.system.services

import org.apache.spark.SparkContext
import org.apache.spark.broadcast.Broadcast

object BroadCastWriter {
  @volatile private var instance: Broadcast[Seq[String]] = null

  def getInstance(sc: SparkContext): Broadcast[Seq[String]] = {
    if (instance == null) {
      synchronized {
        if (instance == null) {
          val wordExcludeList = Seq("a", "b", "c")
          instance = sc.broadcast(wordExcludeList)
        }
      }
    }
    instance
  }
}
