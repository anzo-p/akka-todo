package me.anzop.todo

import akka.http.scaladsl.model.HttpMethods.{DELETE, GET, PATCH, POST, PUT}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.{
  `Access-Control-Allow-Headers`,
  `Access-Control-Allow-Methods`,
  `Access-Control-Allow-Origin`
}
import akka.http.scaladsl.server.Directives.{parameter, _}
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration.DurationInt

trait TodoRoutes extends TodoHandlerProvider with TodoMarshalling {

  implicit val timeout: Timeout = 2 seconds

  def todoRoutes: Route = {
    respondWithHeaders(
      `Access-Control-Allow-Origin`.`*`,
      `Access-Control-Allow-Headers`("Accept", "Content-Type"),
      `Access-Control-Allow-Methods`(GET, POST, PUT, PATCH, DELETE)
    ) {
      pathPrefix("todos" / Segment) { user =>
        pathEndOrSingleSlash {
          parameter('title) { title =>
            get {
              onSuccess(todoHandler(user) ? TodoHandlerActor.GetTodoTasksByTitle(title)) { todos =>
                val body = todos.asInstanceOf[Iterable[TodoTask]].map(todo => TodoTaskDto.fromModel(todo))
                complete(StatusCodes.OK, body)
              }
            }
          } ~ get {
            onSuccess(todoHandler(user) ? TodoHandlerActor.GetAllTodoTasks) { todos =>
              val body = todos.asInstanceOf[Iterable[TodoTask]].map(todo => TodoTaskDto.fromModel(todo))
              complete(StatusCodes.OK, body)
            }
          } ~ post {
            entity(as[TodoTaskDto]) { input =>
              onSuccess(todoHandler(user) ? TodoHandlerActor.AddTodoTask(input.toParams)) { todo =>
                val body = TodoTaskDto.fromModel(todo.asInstanceOf[TodoTask])
                complete(StatusCodes.OK, body)
              }
            }
          }
        } ~ pathPrefix("task" / Segment) { task =>
          pathPrefix("priority" / IntNumber) { priority =>
            pathEndOrSingleSlash {
              patch {
                onSuccess(todoHandler(user) ? TodoHandlerActor.UpdatePriority(task, priority)) { success =>
                  if (success.asInstanceOf[Boolean]) {
                    complete(StatusCodes.OK)
                  } else {
                    complete(StatusCodes.NotFound)
                  }
                }
              }
            }
          } ~ pathPrefix("complete") {
            pathEndOrSingleSlash {
              patch {
                onSuccess(todoHandler(user) ? TodoHandlerActor.UpdateCompleted(task)) { success =>
                  if (success.asInstanceOf[Boolean]) {
                    complete(StatusCodes.OK)
                  } else {
                    complete(StatusCodes.NotFound)
                  }
                }
              }
            }
          } ~ pathEndOrSingleSlash {
            delete {
              onSuccess(todoHandler(user) ? TodoHandlerActor.RemoveTask(task)) { success =>
                if (success.asInstanceOf[Boolean]) {
                  complete(StatusCodes.OK)
                } else {
                  complete(StatusCodes.NotFound)
                }
              }
            }
          }
        }
      }
    }
  }
}
