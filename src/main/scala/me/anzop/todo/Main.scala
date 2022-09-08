package me.anzop.todo

import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContextExecutor
import scala.util._

object Main extends App with TodoHandler with TodoRoutes {

  val port = Properties.envOrElse("PORT", "8080").toInt

  implicit val system: ActorSystem                = ActorSystem()
  implicit val executor: ExecutionContextExecutor = system.dispatcher
  implicit val materializer                       = ActorMaterializer()

  val routes: Route = todoRoutes

  Http(system)
    .bindAndHandle(routes, "0.0.0.0", port = port)
    .foreach(binding => system.log.info("Bound to " + binding.localAddress))
}
