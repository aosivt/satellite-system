package org.satellite.system.core.db.table
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import slick.jdbc.PostgresProfile.api._
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._


final case class Bank(id: Option[Long], bankbik:Option[String], koraccount:Option[String], name:Option[String], inn:Option[String], bankState:Option[String])


class BankTable(tag: Tag) extends Table[Bank](tag,Some("cbo"),"banks"){
  def id = column[Option[Long]]("id")
  def bankbik = column[Option[String]]("bankbik")
  def koraccount = column[Option[String]]("koraccount")
  def name = column[Option[String]]("nname")
  def nameshort = column[Option[String]]("nameshort")
  def inn = column[Option[String]]("inn")
  def bankState = column[Option[String]]("bank_state")
  def * = (id, bankbik,koraccount,name,inn,bankState) <> (Bank.tupled, Bank.unapply)
}

object banks extends TableQuery(new BankTable(_)){
  // will generate sql like:
  //   select * from banks where id = ?
  def byId(ids: Long*): Query[BankTable, Bank, Seq] = banks
    .filter(_.id inSetBind ids)
    .map(t => t)

}