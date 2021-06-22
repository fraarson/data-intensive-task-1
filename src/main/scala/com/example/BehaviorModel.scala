package com.example

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}

import java.util.Date

object BehaviorModel {

  def notifyTemperatureAlarm(signal: TemperatureSignal)(implicit context: ActorContext[TemperatureSignal]): Alarm[Signal] = {
    context.log.info("Temp signal {}", signal)
    new Alarm[Signal](Math.random().intValue(), new Date().getTime, new Date(Int.MaxValue).getTime, signal, signal.sourceSensor)
  }

  def notifyPressureAlarm(signal: PressureSignal)(implicit context: ActorContext[PressureSignal]): Alarm[Signal] = {
    context.log.info("Pressure signal {}", signal)
    new Alarm[Signal](Math.random().intValue(), new Date().getTime, new Date(Int.MaxValue).getTime, signal, signal.sourceSensor)
  }

  def notifyHumidityAlarm(signal: HumiditySignal)(implicit context: ActorContext[HumiditySignal]): Alarm[Signal] = {
    context.log.info("Humidity signal {}", signal)
    new Alarm[Signal](Math.random().intValue(), new Date().getTime, new Date(Int.MaxValue).getTime, signal, signal.sourceSensor)
  }

  def notifyOtherAlarm(): Alarm[UnknownSignal] = new Alarm[UnknownSignal]()

  def apply[S <: Signal](): Behavior[S] = Behaviors.receive { (context, signal) =>
    context.log.info("Signal type: {}", signal.getClass.getSimpleName)
    context.log.info("Signal health: {}", signal.health)
    signal match {
      case h: HumiditySignal => notifyHumidityAlarm(h)(context.asInstanceOf[ActorContext[HumiditySignal]])
      case p: PressureSignal => notifyPressureAlarm(p)(context.asInstanceOf[ActorContext[PressureSignal]])
      case t: TemperatureSignal => notifyTemperatureAlarm(t)(context.asInstanceOf[ActorContext[TemperatureSignal]])
      case _ => notifyOtherAlarm()
    }
    Behaviors.same
  }

}