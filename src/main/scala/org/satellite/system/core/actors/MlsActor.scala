package org.satellite.system.core.actors

import akka.actor.AbstractActor.Receive
import akka.actor.{Actor, ActorRef, Props, Stash}
import akka.event.Logging
import akka.stream.Materializer

object MlsActor {
  def props(): Props = Props(new MlsActor())
}

class MlsActor () extends Actor  {
  private val logger = Logging(context.system, this)
  var dest1: ActorRef = _
  var dest2: ActorRef = _
  def receive = {
    case (d1: ActorRef, d2: ActorRef) =>
      dest1 = d1
      dest2 = d2
    case x =>
      dest1 ! x
      dest2 ! x
  }
}
