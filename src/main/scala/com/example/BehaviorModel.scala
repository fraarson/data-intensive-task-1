package com.example

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}

import java.util.Date

object SignalOrchestrator {

  def notifyTemperatureAlarm(signal: Signal)(implicit context: ActorContext[Signal]): Alarm[Signal] = {
    context.log.info("Temp signal {}", signal)
    new Alarm[Signal](Math.random().intValue(), new Date().getTime, new Date(Int.MaxValue).getTime, signal)
  }

  def notifyPressureAlarm(signal: Signal)(implicit context: ActorContext[Signal]): Alarm[Signal] = {
    context.log.info("Pressure signal {}", signal)
    new Alarm[Signal](Math.random().intValue(), new Date().getTime, new Date(Int.MaxValue).getTime, signal)
  }

  def notifyHumidityAlarm(signal: Signal)(implicit context: ActorContext[Signal]): Alarm[Signal] = {
    context.log.info("Humidity signal {}", signal)
    new Alarm[Signal](Math.random().intValue(), new Date().getTime, new Date(Int.MaxValue).getTime, signal)
  }

  def notifyOtherAlarm(signal: Signal): Alarm[Any] = new Alarm[Any](1, 1, 1, AnyRef)

  def apply(): Behavior[Signal] = Behaviors.receive { (context, signal) =>
    context.log.info("Signal type: {}", signal.getType)
    context.log.info("Signal health: {}", signal.health)
    signal match {
      case HumiditySignal(_) => notifyHumidityAlarm(signal)(context)
      case PressureSignal(_) => notifyPressureAlarm(signal)(context)
      case TemperatureSignal(_) => notifyTemperatureAlarm(signal)(context)
      case _ => throw new IllegalArgumentException
    }
    Behaviors.same
  }

}