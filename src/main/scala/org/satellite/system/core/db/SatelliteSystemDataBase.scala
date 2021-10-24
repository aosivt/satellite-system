package org.satellite.system.core.db

import org.satellite.system.Main.app
import org.satellite.system.core.Application
import slick.jdbc.JdbcBackend
import org.satellite.system.core.db.SatelliteSystemPgProfile.api._
object SatelliteSystemDataBase {
  implicit val app: Application = Application()
  def apply(): SatelliteSystemPgProfile.backend.DatabaseDef = {

    app.config.getString("scalaxdb.properties.driver")
    app.config.getString("scalaxdb.properties.url")
//    Database.forConfig("reference.conf")
    Database.forURL(app.config.getString("scalaxdb.properties.url"),
                    app.config.getString("scalaxdb.properties.user"),
                    app.config.getString("scalaxdb.properties.password"),
                    driver = app.config.getString("scalaxdb.dataSourceClass"))
  }
}
