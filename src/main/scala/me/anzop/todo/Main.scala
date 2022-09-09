package me.anzop.todo

import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route

import scala.concurrent.ExecutionContextExecutor
import scala.util._

object Main extends App with TodoHandler with TodoRoutes {

  implicit val system: ActorSystem                = ActorSystem()
  implicit val executor: ExecutionContextExecutor = system.dispatcher

  val routes: Route = todoRoutes

  Http(system)
    .newServerAt("0.0.0.0", Properties.envOrElse("PORT", "8080").toInt)
    .bindFlow(routes)
    .foreach(binding => system.log.info("Bound to " + binding.localAddress))
}
