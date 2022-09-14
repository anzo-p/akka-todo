package me.anzop.todo

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.{`Access-Control-Allow-Headers`, `Access-Control-Allow-Methods`, `Access-Control-Allow-Origin`}
import akka.http.scaladsl.server.Directives.{parameter, _}
import akka.http.scaladsl.server.{Route, StandardRoute}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration.DurationInt

trait TodoRoutes extends TodoHandlerProvider with TodoMarshalling {

  implicit val timeout: Timeout = 2 seconds

  def itemList(todos: Iterable[TodoTask]): Iterable[TodoTaskDto] =
    todos.map(todo => TodoTaskDto.fromModel(todo))

  def updateSuccessOrNotFound(rowsAffected: Int): StandardRoute =
    rowsAffected match {
      case 0 =>
        complete(StatusCodes.NotFound)
      case _ =>
        complete(StatusCodes.OK)
    }

  def todoRoutes: Route = {
    respondWithHeaders(
      `Access-Control-Allow-Origin`.`*`,
      `Access-Control-Allow-Headers`("Accept", "Content-Type"),
      `Access-Control-Allow-Methods`(GET, POST, PUT, PATCH, DELETE)
    ) {
      pathPrefix("api" / "v1" / "todos" / Segment) { user =>
        pathEndOrSingleSlash {
          parameter('title) { title =>
            get {
              val query = TodoHandlerActor.GetTodoTasksByTitle(title)
              onSuccess((todoHandler(user) ? query).mapTo[Iterable[TodoTask]]) { todos =>
                complete(StatusCodes.OK, itemList(todos))
              }
            }
          } ~ get {
            val query = TodoHandlerActor.GetAllTodoTasks
            onSuccess((todoHandler(user) ? query).mapTo[Iterable[TodoTask]]) { todos =>
              complete(StatusCodes.OK, itemList(todos))
            }
          } ~ post {
            entity(as[TodoTaskDto]) { input =>
              val query = TodoHandlerActor.AddTodoTask(input.toParams)
              onSuccess((todoHandler(user) ? query).mapTo[TodoTask]) { todo =>
                complete(StatusCodes.Created, itemList(List(todo)))
              }
            }
          }
        } ~ pathPrefix("task" / Segment) { task =>
          pathPrefix("priority" / IntNumber) { priority =>
            pathEndOrSingleSlash {
              patch {
                val query = TodoHandlerActor.UpdatePriority(task, priority)
                onSuccess((todoHandler(user) ? query).mapTo[Int]) {
                  updateSuccessOrNotFound
                }
              }
            }
          } ~ pathPrefix("complete") {
            pathEndOrSingleSlash {
              patch {
                val cmd = TodoHandlerActor.UpdateCompleted(task)
                onSuccess((todoHandler(user) ? cmd).mapTo[Int]) {
                  updateSuccessOrNotFound
                }
              }
            }
          } ~ pathEndOrSingleSlash {
            delete {
              val query = TodoHandlerActor.RemoveTask(task)
              onSuccess((todoHandler(user) ? query).mapTo[Int]) {
                updateSuccessOrNotFound
              }
            }
          }
        }
      }
    }
  }
}
