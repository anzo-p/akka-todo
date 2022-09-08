package me.anzop.todo

import akka.http.scaladsl.model.HttpMethods.{GET, PATCH, POST, PUT}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.{
  `Access-Control-Allow-Headers`,
  `Access-Control-Allow-Methods`,
  `Access-Control-Allow-Origin`
}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration.DurationInt

trait TodoRoutes extends TodoHandlerProvider with TodoMarshalling {

  implicit val timeout: Timeout = 2 seconds

  def todoRoutes: Route = {
    (
      respondWithHeaders(
        `Access-Control-Allow-Origin`.`*`,
        `Access-Control-Allow-Headers`("Accept", "Content-Type"),
        `Access-Control-Allow-Methods`(GET, POST, PUT, PATCH)
      ) & extract(_.request.getUri())
    ) { uri =>
      (pathPrefix("todos") & path(Segment)) { id =>
        pathEnd {
          get {
            onSuccess(todoHandler(id) ? TodoHandlerActor.GetAllTodoLists) { todos =>
              val body = todos.asInstanceOf[Iterable[TodoTask]].map(todo => TodoTaskDto.fromModel(todo))
              complete(StatusCodes.OK, body)
            }
          }
        } ~ post {
          entity(as[TodoTaskDto]) { input =>
            onSuccess(todoHandler(id) ? TodoHandlerActor.AddTodoList(input.toParams)) { todos =>
              val body = todos.asInstanceOf[Iterable[TodoTask]].map(todo => TodoTaskDto.fromModel(todo))
              complete(StatusCodes.OK, body)
            }
          }
        }
      }
    } ~
      path("") {
        get {
          complete(StatusCodes.NotFound)
        }
      }
  }
}
