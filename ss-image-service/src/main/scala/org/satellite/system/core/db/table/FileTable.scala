package org.satellite.system.core.db.table

import org.satellite.system.core.db.DBComponent
import slick.lifted.Tag
import org.satellite.system.core.db.SatelliteSystemPgProfile.api._

import scala.concurrent.Future

trait FileRepository extends FileTable { this: DBComponent =>

  import driver.api._

  def create(file: File): Future[Long] = db.run { fileTableAutoInc += file }

  def update(file: File): Future[Int] = db.run { fileTableQuery.filter(_.id === file.id).update(file) }

  def getById(id: Long): Future[Option[File]] = db.run { fileTableQuery.filter(_.id === id).result.headOption }

  def getAll: Future[List[File]] = db.run { fileTableQuery.to[List].result }

  def delete(id: Long): Future[Int] = db.run { fileTableQuery.filter(_.id === id).delete }

}

trait FileTable { this: DBComponent =>

  import driver.api._

  private[FileTable] class FileTable(tag: Tag) extends Table[File](tag,Some("wrk"),"file") {
    val id = column[Long]("file_id", O.PrimaryKey, O.AutoInc)
    val filePath = column[String]("file_path")
    def * = (id,filePath ) <> (File.tupled, File.unapply)
  }

  protected val fileTableQuery = TableQuery[FileTable]

  protected def fileTableAutoInc: driver.ReturningInsertActionComposer[File, Long]
  = fileTableQuery returning fileTableQuery.map(_.id)

}
case class File(id:Long,filePath:String)

