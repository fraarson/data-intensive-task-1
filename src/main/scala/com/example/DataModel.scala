package com.example

trait Signal {
  def health: Boolean

  def sourceSensor: Sensor
}

case class PressureSignal(override val health: Boolean, override val sourceSensor: Sensor) extends Signal

case class TemperatureSignal(override val health: Boolean, override val sourceSensor: Sensor) extends Signal

case class HumiditySignal(override val health: Boolean, override val sourceSensor: Sensor) extends Signal

case class EmptySignal(override val health: Boolean = false, override val sourceSensor: Sensor = new Sensor()) extends Signal

class SensorLocation(val id: Int = 0, val zipCode: String = "")

class Sensor(val id: Int = 0, location: SensorLocation = new SensorLocation())

class Alarm[S <: Signal](val id: Int = 0, val startDateTime: Long = 0L, val endDateTime: Long = 0L,
                         val signal: S = EmptySignal(), val sensor: Sensor = new Sensor())