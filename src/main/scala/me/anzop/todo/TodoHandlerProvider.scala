package me.anzop.todo

import akka.actor.ActorRef

trait TodoHandlerProvider {
  def todoHandler(userId: String): ActorRef
}
