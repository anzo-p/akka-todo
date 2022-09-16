package me.anzop.todo

import akka.actor._
import akka.http.scaladsl.Http
import me.anzop.todo.actor.TodoHandlerResolver
import me.anzop.todo.http.TodoRoutes

import scala.concurrent.ExecutionContextExecutor
import scala.util._

object Main extends App with TodoHandlerResolver {

  implicit val system: ActorSystem                = ActorSystem("TodoAppActorSystem")
  implicit val executor: ExecutionContextExecutor = system.dispatcher

  val todoRoutes = new TodoRoutes((userId: String) => todoHandler(userId))

  Http(system)
    .newServerAt("0.0.0.0", Properties.envOrElse("PORT", "8080").toInt)
    .bindFlow(todoRoutes.routes)
    .foreach(binding => system.log.info("Bound to " + binding.localAddress))
}
