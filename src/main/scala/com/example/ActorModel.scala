package com.example

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.DurationInt
import scala.util.Random

object ActorModel {

  class SensorDeviceActor[S <: Signal](deviceId: Int) extends Actor with ActorLogging {

    val storageActor: ActorRef = context.actorOf(Props(StorageActor), "StorageActor")
    val signalsBatch: ListBuffer[S] = new ListBuffer[S]()

    implicit val executionContext: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
    implicit val currentAlarm: Option[Alarm[S]] = None

    context.system.scheduler.scheduleWithFixedDelay(60.seconds, 60.second) {
      () => {
        synchronized(signalsBatch) {
          val sortedSignalsBatch = signalsBatch.toList.sortBy(it => it.timestamp)
          storageActor ! sortedSignalsBatch
          signalsBatch.clear()
          val (closePreviousAlarm, newAlarm) = checkSignalsForAlarms(sortedSignalsBatch)
          0
        }
      }
    }

    def checkSignalsForAlarms(signalsBatch: List[S])(implicit currentAlarm: Option[Alarm[S]]): (Boolean, Option[Alarm[S]]) = {
      signalsBatch.reduceLeft { (acc: (Option[Alarm[S]], Option[Alarm[S]]), signal: S) => {
        if (!signal.health && acc._1.isDefined) {

        }

        //
        //
        //        case p.isLeft =>
        //          val currentAlarm = p.left.getOrElse(Alarm())
        //          if (!n.health) {
        //            if (currentAlarm.startDateTime != 0) {
        //              Left(Alarm(currentAlarm.id, currentAlarm.startDateTime, n.timestamp, n, n.sourceSensor))
        //            } else {
        //              Left(Alarm(Random.nextInt(), n.timestamp, n.timestamp, n, n.sourceSensor))
        //            }
        //          } else {
        //            Right
        //          }
        //        case p.isRight =>
        //          if (!n.health) {
        //            Left(Alarm(Random.nextInt(), n.timestamp, n.timestamp, n, n.sourceSensor))
        //          } else {
        //            Right
        //          }
        //      }
      }
      }
    }

    override def receive: Receive = {
      case s: S =>
        log.info(s"$deviceId device received signal $s")
        signalsBatch.append(s)
      case o => log.error("Unsupported type {}", o.getClass)
    }

  }

  class GuardianActor extends Actor with ActorLogging {

    override def receive: Receive = {
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
