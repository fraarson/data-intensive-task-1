package com.example

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

import java.util.Date
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.DurationInt
import scala.util.Random

object ActorModel {

  class SensorDeviceActor[S <: Signal](deviceId: Int) extends Actor with ActorLogging {

    val signalsBatch: ListBuffer[S] = new ListBuffer[S]()
    var currentAlarm: Option[Alarm[S]] = None

    val storageActor: ActorRef = context.actorOf(Props(StorageActor), "StorageActor")

    implicit val executionContext: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

    context.system.scheduler.scheduleWithFixedDelay(60.seconds, 60.second) {
      () => {
        synchronized(signalsBatch) {
          val sortedSignalsBatch = signalsBatch.toList.sortBy(it => it.timestamp)
          storageActor ! sortedSignalsBatch
          signalsBatch.clear()
          checkForAlarms(sortedSignalsBatch) match {
            case Left(value) => currentAlarm = Option.apply(value)
            case Right(_) => currentAlarm = None
          }
          0
        }
        if (currentAlarm.isDefined) {

        }
      }
    }

    def checkForAlarms(signalsBatch: List[S]): Either[Alarm[S], Nothing] = {
      val foundSignal = signalsBatch.find(it => !it.health).orNull
      if (foundSignal != null) {
        Alarm(Random.nextInt(), new Date().getTime, 0L, foundSignal, foundSignal.sourceSensor)
      }
    }

    def receive: Receive = {
      case s: S =>
        log.info(s"$deviceId device received signal $s")
        signalsBatch.append(s)
      case _ => throw new IllegalStateException()
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

    override def receive: Receive = {
      case signal: Signal =>
        log.info("LocationActor invoked zip: {}", zipCode)
        val deviceId = signal.sourceSensor.id
        val childOption: Option[ActorRef] = context.child(String.valueOf(deviceId))
        val deviceActor: ActorRef = if (childOption.isDefined) {
          childOption.get
        } else {
          context.actorOf(Props(new SensorDeviceActor[signal.type](deviceId)), String.valueOf(deviceId))
        }
        deviceActor ! signal
    }
  }

  object StorageActor extends Actor with ActorLogging {

    override def receive: Receive = {
      case signalsList: List[Signal] =>
        log.info("alarm stored! {}", signalsList.map { it => it.timestamp })
      case alarm: Alarm[Signal] => log.info(s"ALARM ALARM ALARM $alarm")
    }

  }

}
