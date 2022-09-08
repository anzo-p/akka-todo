package me.anzop.todo

import akka.stream.Materializer

trait FlowMaterializerProvider {
  implicit val materializer: Materializer
}
