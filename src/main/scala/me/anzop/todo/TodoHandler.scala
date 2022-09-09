package me.anzop.todo

import akka.actor.{ActorRef, ActorSystem, Cancellable, Props}

import scala.concurrent.duration.{DurationInt, FiniteDuration}

trait TodoHandler {
  implicit val system: ActorSystem

  val actorLifetime: FiniteDuration       = 10 seconds
  var activeActors: Map[String, ActorRef] = Map()
  var timeouts: Map[String, Cancellable]  = Map()

  def scheduleTimeout(name: String, actor: ActorRef): Cancellable =
    system.scheduler.scheduleOnce(actorLifetime, passivate(name, actor))(system.dispatcher)

  def passivate(name: String, actor: ActorRef): Runnable = () => {
    actor ! TodoHandlerActor.Shutdown
    activeActors -= name
    timeouts     -= name
    system.log.info(s"passivating actor $name, state now: $activeActors")
  }

  def todoHandler(userId: String): ActorRef = {
    val wouldBeActor = s"todo-handler-$userId"

    if (activeActors.contains(wouldBeActor)) {
      system.log.info(s"discovered existing actor ${activeActors(wouldBeActor)}")
      val actor = activeActors(wouldBeActor)
      timeouts(wouldBeActor).cancel()
      timeouts += wouldBeActor -> scheduleTimeout(wouldBeActor, actor)
      actor

    } else {
      val actor = system.actorOf(Props(new TodoHandlerActor(userId)), wouldBeActor)
      system.log.info(s"created new actor $actor")
      activeActors += wouldBeActor -> actor
      timeouts     += wouldBeActor -> scheduleTimeout(wouldBeActor, actor)
      actor
    }
  }
}
