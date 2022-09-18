package todoapp.actor

import akka.actor.{ActorRef, Props}
import todoapp.system.ActorResolver

import java.util.UUID
import scala.concurrent.duration.{DurationInt, FiniteDuration}

trait TodoHandlerResolver extends ActorResolver {

  val actorLifetime: FiniteDuration = 3 minutes

  def createNewActor(selector: String, actorName: String): ActorRef =
    system.actorOf(Props(new TodoHandlerActor(UUID.fromString(selector))), actorName)

  def shutDownActor(ref: ActorRef): Unit =
    ref ! TodoHandlerActor.Shutdown

  def todoHandler(selector: String): ActorRef = {
    resolveActor(selector, s"todo-handler-$selector")
  }
}
