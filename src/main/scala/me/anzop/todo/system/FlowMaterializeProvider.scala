package me.anzop.todo.system

import akka.stream.Materializer

trait FlowMaterializeProvider {
  implicit val materialize: Materializer
}
