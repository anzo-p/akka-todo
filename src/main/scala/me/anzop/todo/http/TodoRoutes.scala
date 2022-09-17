package me.anzop.todo.http

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.{
  `Access-Control-Allow-Headers`,
  `Access-Control-Allow-Methods`,
  `Access-Control-Allow-Origin`
}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, StandardRoute}
import me.anzop.todo.TodoService
import me.anzop.todo.http.dto.TodoTaskDto.toList
import me.anzop.todo.http.dto._
import me.anzop.todo.http.system.BaseRoute

class TodoRoutes(val todoService: TodoService) extends BaseRoute {

  private def updateSuccessOrNotFound(rowsAffected: Int): StandardRoute =
    rowsAffected match {
      case 0 =>
        complete(StatusCodes.NotFound)
      case _ =>
        complete(StatusCodes.OK)
    }

  def routes: Route = {
    respondWithHeaders(
      `Access-Control-Allow-Origin`.`*`,
      `Access-Control-Allow-Headers`("Accept", "Content-Type"),
      `Access-Control-Allow-Methods`(GET, POST, PUT, PATCH, DELETE)
    ) {
      pathPrefix("api" / "v1" / "todos" / JavaUUID) { user =>
        pathEndOrSingleSlash {
          parameter('title) { title =>
            get {
              onSuccess(todoService.getAllTodosByTitle(user, title)) { todos =>
                complete(StatusCodes.OK, toList(todos))
              }
            }
          } ~ get {
            onSuccess(todoService.getAllTodos(user)) { todos =>
              complete(StatusCodes.OK, toList(todos))
            }
          } ~ post {
            entity(as[TodoTaskDto]) { payload =>
              validateRequest(payload) {
                onSuccess(todoService.addTodo(user, payload.toParams)) { todo =>
                  complete(StatusCodes.Created, toList(List(todo)))
                }
              }
            }
          }
        } ~ pathPrefix("task" / JavaUUID) { task =>
          pathPrefix("priority") {
            pathEndOrSingleSlash {
              patch {
                entity(as[TodoPriorityDto]) { payload =>
                  validateRequest(payload) {
                    onSuccess(todoService.updatePriority(user, task, payload.priority)) {
                      updateSuccessOrNotFound
                    }
                  }
                }
              }
            }
          } ~ pathPrefix("completed") {
            pathEndOrSingleSlash {
              patch {
                onSuccess(todoService.updateCompleted(user, task)) {
                  updateSuccessOrNotFound
                }
              }
            }
          } ~ pathEndOrSingleSlash {
            delete {
              onSuccess(todoService.removeTask(user, task)) {
                updateSuccessOrNotFound
              }
            }
          }
        }
      }
    }
  }
}
