package org.satellite.system.services

import org.satellite.system.core.Application

trait SocketConnector extends SparkSocket {
  implicit val app: Application
}
