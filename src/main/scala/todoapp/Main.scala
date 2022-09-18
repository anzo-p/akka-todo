package todoapp

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import todoapp.actor.TodoHandlerResolver
import todoapp.http.TodoRoutes

import java.util.UUID
import scala.concurrent.ExecutionContextExecutor
import scala.util.Properties

object Main extends App with TodoHandlerResolver {

  implicit val system: ActorSystem                = ActorSystem("TodoAppActorSystem")
  implicit val executor: ExecutionContextExecutor = system.dispatcher

  val todoRoutes = new TodoRoutes((selector: UUID) => todoHandler(selector.toString))

  Http(system)
    .newServerAt("0.0.0.0", Properties.envOrElse("PORT", "8080").toInt)
    .bindFlow(todoRoutes.routes)
    .foreach(binding => system.log.info("Bound to " + binding.localAddress))
}
