package com.example

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

object ActorModel {

  class SensorDeviceActor(deviceId: Int) extends Actor with ActorLogging {

    def receive: Receive = {
      case _: Signal => log.error("not implemented!")
    }

  }

  class GuardianActor extends Actor with ActorLogging {

    def receive: Receive = {
      case signal: Signal =>
        log.info("Signal type: {}", signal.getClass.getSimpleName)
        log.info("Signal health: {}", signal.health)
        val sensorZipCode = signal.sourceSensor.location.zipCode
        val childOption: Option[ActorRef] = context.child(String.valueOf(sensorZipCode))
        val locationActor: ActorRef = if (childOption.isDefined) {
          childOption.get
        } else {
          context.actorOf(Props(new LocationActor(sensorZipCode)), String.valueOf(sensorZipCode))
        }
        locationActor ! signal
    }

  }

  class LocationActor(zipCode: String) extends Actor with ActorLogging {

    override def receive: Receive = { _ =>
      log.info("LocationActor invoked zip: {}", zipCode)
    }
  }

  class StorageActor extends Actor with ActorLogging {

    override def receive: Receive = { alarm =>
      log.info("alarm stored! {}", alarm)
    }

  }

}
