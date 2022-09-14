package me.anzop.todo

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.{
  `Access-Control-Allow-Headers`,
  `Access-Control-Allow-Methods`,
  `Access-Control-Allow-Origin`
}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, StandardRoute}

trait TodoRoutes extends TodoMarshalling with TodoService {

  private def updateSuccessOrNotFound(rowsAffected: Int): StandardRoute =
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
              onSuccess(getAllTodos(user, title)) { todos =>
                complete(StatusCodes.OK, toList(todos))
              }
            }
          } ~ get {
            onSuccess(getTodo(user)) { todos =>
              complete(StatusCodes.OK, toList(todos))
            }
          } ~ post {
            entity(as[TodoTaskDto]) { payload =>
              onSuccess(addTodo(user, payload)) { todo =>
                complete(StatusCodes.Created, toList(List(todo)))
              }
            }
          }
        } ~ pathPrefix("task" / Segment) { task =>
          pathPrefix("priority") {
            pathEndOrSingleSlash {
              patch {
                entity(as[TodoPriorityDto]) { payload =>
                  onSuccess(updatePriority(user, task, payload)) {
                    updateSuccessOrNotFound
                  }
                }
              }
            }
          } ~ pathPrefix("complete") {
            pathEndOrSingleSlash {
              patch {
                onSuccess(updateCompleted(user, task)) {
                  updateSuccessOrNotFound
                }
              }
            }
          } ~ pathEndOrSingleSlash {
            delete {
              onSuccess(removeTask(user, task)) {
                updateSuccessOrNotFound
              }
            }
          }
        }
      }
    }
  }
}
