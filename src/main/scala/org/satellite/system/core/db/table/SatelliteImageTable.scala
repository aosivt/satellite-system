package org.satellite.system.core.db.table

import slick.lifted.Tag
import org.satellite.system.core.db.SatelliteSystemPgProfile.api._

case class SatelliteImage(id: Long, fileId:Long, geoTransform: List[Double], projection: String, name: String, satelliteId:Option[Long])

class SatelliteImageTable(tag: Tag) extends Table[SatelliteImage](tag,Some("dim"),"satellite_image"){
  def id = column[Long]("satellite_image_id")
  def fileId = column[Long]("file_id")
  def projection = column[String]("projection")
  def geoTransform = column[List[Double]]("geo_transform")
  def name = column[String]("name")
  def satelliteId = column[Option[Long]]("satellite_id")
  def * = (id, fileId,geoTransform,projection,name,satelliteId) <> (SatelliteImage.tupled, SatelliteImage.unapply)
  def file = foreignKey("file",fileId,FileService)(_.id)
}

object SatelliteImageService extends TableQuery(new SatelliteImageTable(_)){

}
