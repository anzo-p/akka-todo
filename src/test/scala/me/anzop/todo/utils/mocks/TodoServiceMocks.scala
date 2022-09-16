package me.anzop.todo.utils.mocks

import me.anzop.todo.TodoService
import me.anzop.todo.models.{TodoTask, TodoTaskParams}
import org.scalamock.scalatest.MockFactory

import scala.concurrent.Future

trait TodoServiceMocks { this: MockFactory =>
  protected val mockTodoService = mock[TodoService]

  protected def mockGetAllTodos(user: String, todos: Iterable[TodoTask]) =
    (mockTodoService.getAllTodos _).expects(user).returns(Future.successful(todos))

  protected def mockGetAllTodosByTitle(user: String, title: String, todos: Iterable[TodoTask]) = {
    (mockTodoService.getAllTodosByTitle _).expects(user, title).returns(Future.successful(todos))
  }

  protected def mockAddTodo(user: String, payload: TodoTaskParams, todo: TodoTask) =
    (mockTodoService.addTodo _).expects(user, payload).returns(Future.successful(todo))

  protected def mockUpdatePriority(
      userId: String,
      taskId: String,
      priority: Int,
      result: Int
    ) =
    (mockTodoService.updatePriority _).expects(userId, taskId, priority).returns(Future.successful(result))

  protected def mockUpdateCompleted(userId: String, taskId: String, result: Int) =
    (mockTodoService.updateCompleted _).expects(userId, taskId).returns(Future.successful(result))

  protected def mockRemoveTask(userId: String, taskId: String, result: Int) =
    (mockTodoService.removeTask _).expects(userId, taskId).returns(Future.successful(result))
}
