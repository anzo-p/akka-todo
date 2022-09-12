package me.anzop.todo

import akka.stream.Materializer

trait FlowMaterializeProvider {
  implicit val materialize: Materializer
}
