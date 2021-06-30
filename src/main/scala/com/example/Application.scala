package com.example

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives.{as, complete, entity, path}
import akka.http.scaladsl.server.{Directives, Route}
import com.example.ActorModel.GuardianActor

import scala.io.StdIn

object Application extends App {

  implicit val system: ActorSystem = ActorSystem("SignalsAlarmsActorSystem")

  implicit val guardianActor: ActorRef = system.actorOf(Props(new GuardianActor()), "GuardianActor")

  val route: Route =
    Directives.concat(
      Directives.post {
        path("signal") {
          entity(as[String]) { body =>
            val signal = InputObjectReader.read(body)
            system.log.info(signal.toString)
            guardianActor ! signal
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
  system.terminate()

}
