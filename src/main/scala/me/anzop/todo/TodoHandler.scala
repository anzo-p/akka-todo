package me.anzop.todo

import akka.actor.{ActorRef, ActorSystem, Props}

import scala.concurrent.duration.{DurationInt, FiniteDuration}

trait TodoHandler {
  implicit val system: ActorSystem

  val actorLifetime: FiniteDuration       = 10 seconds
  var activeActors: Map[String, ActorRef] = Map()

  def todoHandler(userId: String): ActorRef = {
    val wouldBeActor = s"todo-handler-$userId"

    if (activeActors.contains(wouldBeActor)) {
      system.log.info(s"discovered existing actor ${activeActors(wouldBeActor)}")
      activeActors(wouldBeActor)
    } else {
      val actor = system.actorOf(Props(new TodoHandlerActor(userId)), wouldBeActor)
      system.log.info(s"created new actor $actor")
      activeActors += (wouldBeActor -> actor)

      val passivate: Runnable = () => {
        actor ! TodoHandlerActor.Shutdown
        activeActors -= wouldBeActor
      }
      system.scheduler.scheduleOnce(actorLifetime, passivate)(system.dispatcher)

      actor
    }
  }
}
