package com.example

import akka.actor.typed.{ ActorRef, ActorSystem, Behavior }

object Application extends App {

  val actorSystem: ActorSystem[Signal] = ActorSystem(SignalOrchestrator(), "Test_actor_system")

  actorSystem ! TemperatureSignal(isAlive = false)

  actorSystem.terminate()

}
