package me.anzop.todo

import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import me.anzop.todo.http.TodoRoutes

import scala.concurrent.ExecutionContextExecutor
import scala.util._

object Main extends App with TodoHandlerResolver with TodoRoutes {

  implicit val system: ActorSystem                = ActorSystem("TodoAppActorSystem")
  implicit val executor: ExecutionContextExecutor = system.dispatcher

  val routes: Route = todoRoutes

  Http(system)
    .newServerAt("0.0.0.0", Properties.envOrElse("PORT", "8080").toInt)
    .bindFlow(routes)
    .foreach(binding => system.log.info("Bound to " + binding.localAddress))
}
