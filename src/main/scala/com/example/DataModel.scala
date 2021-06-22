package com.example

import com.fasterxml.jackson.core.JsonParseException
import play.api.libs.functional.FunctionalBuilder
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json._

import scala.util.Try

sealed abstract class Signal {
  def health: Boolean

  def timestamp: Long

  def sourceSensor: Sensor
}

case class PressureSignal(override val health: Boolean, override val timestamp: Long, override val sourceSensor: Sensor) extends Signal

case class TemperatureSignal(override val health: Boolean, override val timestamp: Long, override val sourceSensor: Sensor) extends Signal

case class HumiditySignal(override val health: Boolean, override val timestamp: Long, override val sourceSensor: Sensor) extends Signal

case class UnknownSignal(override val health: Boolean = false, override val timestamp: Long = 0L, override val sourceSensor: Sensor = Sensor()) extends Signal

case class SensorLocation(id: Int = 0, zipCode: String = "")

case class Sensor(id: Int = 0, location: SensorLocation = SensorLocation())

case class Alarm[S <: Signal](id: Int = 0, startDateTime: Long = 0L, endDateTime: Long = 0L,
                              signal: S = UnknownSignal(), sensor: Sensor = Sensor())

object InputObjectReader {

  implicit val sensorLocationReads: Reads[SensorLocation] = (
    (JsPath \ "id").read[Int] and
      (JsPath \ "zipCode").read[String]
    ) (SensorLocation.apply _)

  implicit val sensorReads: Reads[Sensor] = (
    (JsPath \ "id").read[Int] and
      (JsPath \ "location").read[SensorLocation]
    ) (Sensor.apply _)

  def signalReader: FunctionalBuilder[Reads]#CanBuild3[Boolean, Long, Sensor] = {
    (JsPath \ "health").read[Boolean] and
      (JsPath \ "timestamp").read[Long] and
      (JsPath \ "sourceSensor").read[Sensor]
  }

  implicit val humidityReads: Reads[HumiditySignal] = signalReader(HumiditySignal.apply _)

  implicit val pressureReads: Reads[PressureSignal] = signalReader(PressureSignal.apply _)

  implicit val temperatureSignal: Reads[TemperatureSignal] = signalReader(TemperatureSignal.apply _)


  implicit object SignalReads extends Reads[Signal] {
    override def reads(json: JsValue): JsResult[Signal] = {
      (json \ "type").as[String] match {
        case "Pressure_Signal" => Json.fromJson[PressureSignal](json)
        case "Temperature_Signal" => Json.fromJson[TemperatureSignal](json)
        case "Humidity_Signal" => Json.fromJson[HumiditySignal](json)
        case _ => JsResult.fromTry(Try(UnknownSignal()))
      }
    }
  }

  def read(valueAsString: String): Signal = {
    try {
      Json.fromJson[Signal](Json.parse(valueAsString)).getOrElse(UnknownSignal())
    } catch {
      case _: JsonParseException => UnknownSignal()
    }
  }
}