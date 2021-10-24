package org.satellite.system.core.db.table

import slick.jdbc.PostgresProfile.api._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

case class Article(id:String,name1:Option[String],name2:Option[String],ecrshortname:Option[String],ecrlongname:Option[String])

class ArticleTable(tag: Tag) extends Table[Article](tag,Some("cbo"),"cbo_articles"){
  def id = column[String]("external_str_id")
  def name1 = column[Option[String]]("name1")
  def name2 = column[Option[String]]("name2")
  def ecrshortname = column[Option[String]]("ecrshortname")
  def ecrlongname = column[Option[String]]("ecrlongname")


  def * = (id, name1,name2,ecrshortname,ecrlongname) <> (Article.tupled, Article.unapply)
}

object articles extends TableQuery(new ArticleTable(_)){
  // will generate sql like:
  //   select * from banks where id = ?
//  def byId(ids: Long*): Query[BankTable, Bank, Seq] = banks
//    .filter(_.id inSetBind ids)
//    .map(t => t)
}