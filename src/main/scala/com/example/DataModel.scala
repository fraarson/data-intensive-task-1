package com.example

trait Signal {

  def getType = this

  def health: Boolean
}

class HealthContainer(val isAlive: Boolean)

case class PressureSignal(override val isAlive: Boolean) extends HealthContainer(isAlive)
  with Signal {
  override def health: Boolean = isAlive
}

case class TemperatureSignal(override val isAlive: Boolean) extends HealthContainer(isAlive)
  with Signal {
  override def health: Boolean = isAlive
}

case class HumiditySignal(override val isAlive: Boolean) extends HealthContainer(isAlive)
  with Signal {

  override def health: Boolean = isAlive
}

class SensorLocation(val id: Int, val zipCode: String)

class Sensor[T](val id: Int, location: SensorLocation) {
  def produceSignal(): Signal = ???
}

class Alarm[ST](val id: Int, val startDateTime: Long, val endDateTime: Long, val signalType: ST)