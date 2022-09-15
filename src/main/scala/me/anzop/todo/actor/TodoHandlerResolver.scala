package me.anzop.todo.actor

import akka.actor.{ActorRef, Props}
import me.anzop.todo.system.ActorResolver

import scala.concurrent.duration.{DurationInt, FiniteDuration}

trait TodoHandlerResolver extends ActorResolver {

  val actorLifetime: FiniteDuration = 3 minutes

  def createNewActor(selector: String, actorName: String): ActorRef =
    system.actorOf(Props(new TodoHandlerActor(selector)), actorName)

  def shutDownActor(ref: ActorRef): Unit =
    ref ! TodoHandlerActor.Shutdown

  def todoHandler(selector: String): ActorRef = {
    resolveActor(selector, s"todo-handler-$selector")
  }
}
