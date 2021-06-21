package com.example

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives.{as, complete, entity, path}
import akka.http.scaladsl.server.{Directives, Route}

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object Application extends App {

  implicit val system: ActorSystem[Signal] = ActorSystem(BehaviorModel(), "Signals_alarms_actor_system")

  implicit val executionContext: ExecutionContextExecutor = system.executionContext

  val route: Route =
    Directives.concat(
      Directives.post {
        path("signal") {
          entity(as[String]) { body =>
            system.log.info(body)
            if (body.contains("1")) {
              system ! HumiditySignal(false, new Sensor())
            } else {
              system ! TemperatureSignal(true, new Sensor())
            }
            complete(StatusCodes.Accepted, HttpEntity.Empty)
          }
        }
      },
      Directives.get {
        path("health")
        complete(StatusCodes.OK, "Healthy")
      }
    )

  val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())

}
