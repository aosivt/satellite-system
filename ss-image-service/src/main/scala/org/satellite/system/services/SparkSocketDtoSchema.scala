package org.satellite.system.services

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.streaming.DataStreamWriter
import org.apache.spark.sql.{DataFrame, Row, SaveMode, SparkSession}
import org.apache.spark.sql.types.{ArrayType, DoubleType, IntegerType, StringType, StructField, StructType}
import org.satellite.system.image.converter.core.DtoSparkImagePart

trait SparkSocketDtoSchema {

  val schema = StructType(Array(
    StructField(name = "rowId", dataType = IntegerType, nullable = false),
    StructField(name = "colId", dataType = IntegerType, nullable = false),
    StructField(name = "width", dataType = IntegerType, nullable = false),
    StructField(name = "height", dataType = IntegerType, nullable = false),
    StructField(name = "projection", dataType = StringType, nullable = true),

    StructField(name = "geoTransform", dataType = ArrayType(DoubleType), nullable = false),
    StructField(name = "dataDeepBlue", dataType = ArrayType(DoubleType), nullable = true),
    StructField(name = "dataBlue", dataType = ArrayType(DoubleType), nullable = true),
    StructField(name = "dataGreen", dataType = ArrayType(DoubleType), nullable = true),
    StructField(name = "dataRed", dataType = ArrayType(DoubleType), nullable = true),
    StructField(name = "dataNIR", dataType = ArrayType(DoubleType), nullable = true),
    StructField(name = "dataSWIR1", dataType = ArrayType(DoubleType), nullable = true),
    StructField(name = "dataSWIR2", dataType = ArrayType(DoubleType), nullable = true),
    StructField(name = "dataSWIR3", dataType = ArrayType(DoubleType), nullable = true),
    StructField(name = "dataCirrus", dataType = ArrayType(DoubleType), nullable = true),
    StructField(name = "dataTer", dataType = ArrayType(DoubleType), nullable = true),
    StructField(name = "dataTIRS1", dataType = ArrayType(DoubleType), nullable = true),
    StructField(name = "dataTIRS2", dataType = ArrayType(DoubleType), nullable = true),
    StructField(name = "dataVCID1", dataType = ArrayType(DoubleType), nullable = true),
    StructField(name = "dataVCID2", dataType = ArrayType(DoubleType), nullable = true),
    StructField(name = "name", dataType = StringType, nullable = false),
  ))

  def toDF(setDto: Seq[DtoSparkImagePart])(implicit spark: SparkSession): DataFrame = {
    spark.createDataFrame(convert(setDto),schema)
  }
  def write(setDto: Seq[String])(implicit spark: SparkSession): Unit = {

    jsonToDF(setDto)
      .coalesce(1).write.mode(SaveMode.Append)
      .partitionBy("name")
      .parquet("/media/alex/058CFFE45C3C7827/ss/stg_data_satellite_images")

    println(setDto.length)
    println(setDto.head)

  }
  def jsonToDF(setDto: Seq[String])(implicit spark: SparkSession): DataFrame = {
    jsonConvert(setDto)

  }
  def jsonConvert(setDto: Seq[String])(implicit spark: SparkSession): DataFrame = {
    import spark.implicits._
    spark.read.schema(schema).json(setDto.toDS())
  }
  def convert(setDto: Seq[DtoSparkImagePart])(implicit spark: SparkSession): RDD[Row] = {
    spark.sparkContext.parallelize(setDto.map(convertDto))
  }
  def convertDto(dto: DtoSparkImagePart): Row = {
    Row(
    dto.getRowId,
    dto.getColId,
    dto.getWidth,
    dto.getHeight,
    dto.getProjection,
    dto.getGeoTransform,
    dto.getDataDeepBlue,
    dto.getDataBlue,
    dto.getDataGreen,
    dto.getDataRed,
    dto.getDataNIR,
    dto.getDataSWIR1,
    dto.getDataSWIR2,
    dto.getDataSWIR3,
    dto.getDataCirrus,
    dto.getDataTer,
    dto.getDataTIRS1,
    dto.getDataTIRS2,
    dto.getDataVCID1,
    dto.getDataVCID2,
    )
  }
}
