package org.satellite.system.core.db

trait DBComponent {
  val driver: SatelliteSystemPgProfile = SatelliteSystemPgProfile

  import driver.api._

  val db: Database = SatelliteSystemDataBase.apply()
}
