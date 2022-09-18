package todoapp

import akka.actor.ActorRef

import java.util.UUID

trait TodoHandlerProvider {
  def todoHandler(userId: UUID): ActorRef
}
