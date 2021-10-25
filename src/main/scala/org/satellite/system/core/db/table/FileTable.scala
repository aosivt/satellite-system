package org.satellite.system.core.db.table

import slick.lifted.Tag
import org.satellite.system.core.db.SatelliteSystemPgProfile.api._

case class File(id:Long,filePath:String)

class FileTable (tag: Tag) extends Table[File](tag,Some("wrk"),"file"){
  def id = column[Long]("file_id")
  def filePath = column[String]("file_path")
  def * = (id, filePath) <> (File.tupled, File.unapply)
}

object FileService extends TableQuery(new FileTable(_)){

}
