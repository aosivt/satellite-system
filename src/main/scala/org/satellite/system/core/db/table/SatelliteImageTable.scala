package org.satellite.system.core.db.table

import org.satellite.system.core.db.DBComponent

import scala.concurrent.Future

case class SatelliteImage(id: Long, fileId:Long, geoTransform: List[Double],
                          projection: String, name: String, satelliteId:Option[Long])

trait SatelliteImageRepository extends SatelliteImageTable { this: DBComponent =>

  import driver.api._

  def create(satelliteImage: SatelliteImage): Future[Int]
  = db.run { satelliteImageTableQuery += satelliteImage }

  def update(satelliteImage: SatelliteImage): Future[Int]
  = db.run { satelliteImageTableQuery.filter(_.id === satelliteImage.id).update(satelliteImage) }

  def getById(id: Long): Future[Option[SatelliteImage]]
  = db.run { satelliteImageTableQuery.filter(_.id === id).result.headOption }

  def getAll: Future[List[SatelliteImage]]
  = db.run { satelliteImageTableQuery.to[List].result }

  def delete(id: Long): Future[Int]
  = db.run { satelliteImageTableQuery.filter(_.id === id).delete }

  def getSatelliteImageWithFile: Future[List[(File, SatelliteImage)]] =
    db.run {
      (for {
        info <- satelliteImageTableQuery
        bank <- info.file
      } yield (bank, info)).to[List].result
    }


  def getAllSatelliteImageWithFile: Future[List[(File, Option[SatelliteImage])]] =
    db.run {
      fileTableQuery.joinLeft(satelliteImageTableQuery).on(_.id === _.fileId).to[List].result
    }
}

trait SatelliteImageTable extends FileTable { this: DBComponent =>

  import driver.api._

  private[SatelliteImageTable] class SatelliteImageTable(tag: Tag)
    extends Table[SatelliteImage](tag,Some("dim"),"satellite_image") {
      def id = column[Long]("satellite_image_id", O.PrimaryKey, O.AutoInc)
      def fileId = column[Long]("file_id")
      def projection = column[String]("projection")
      def geoTransform = column[List[Double]]("geo_transform")
      def name = column[String]("name")
      def satelliteId = column[Option[Long]]("satellite_id")
      def file = foreignKey("file_id",fileId,fileTableQuery)(_.id)
      def * = (id, fileId,geoTransform,projection,name,satelliteId) <>
        (SatelliteImage.tupled, SatelliteImage.unapply)
  }

  protected val satelliteImageTableQuery = TableQuery[SatelliteImageTable]

  protected def satelliteImageTableAutoInc: driver.ReturningInsertActionComposer[SatelliteImage, Long]
  = satelliteImageTableQuery returning satelliteImageTableQuery.map(_.id)

}

