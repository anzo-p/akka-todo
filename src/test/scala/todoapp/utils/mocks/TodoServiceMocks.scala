package todoapp.utils.mocks

import org.scalamock.scalatest.MockFactory
import todoapp.TodoService
import todoapp.models.{TodoTask, TodoTaskParams}

import java.util.UUID
import scala.concurrent.Future

trait TodoServiceMocks { this: MockFactory =>
  protected val mockTodoService = mock[TodoService]

  protected def mockGetAllTodos(user: UUID, todos: Iterable[TodoTask]) =
    (mockTodoService.getAllTodos _).expects(user).returns(Future.successful(todos))

  protected def mockGetAllTodosByTitle(user: UUID, title: String, todos: Iterable[TodoTask]) = {
    (mockTodoService.getAllTodosByTitle _).expects(user, title).returns(Future.successful(todos))
  }

  protected def mockAddTodo(user: UUID, params: TodoTaskParams, todo: TodoTask) =
    (mockTodoService.addTodo _).expects(user, params).returns(Future.successful(todo))

  protected def mockUpdatePriority(
      userId: UUID,
      taskId: UUID,
      priority: Int,
      result: Int
    ) =
    (mockTodoService.updatePriority _).expects(userId, taskId, priority).returns(Future.successful(result))

  protected def mockUpdateCompleted(userId: UUID, taskId: UUID, result: Int) =
    (mockTodoService.updateCompleted _).expects(userId, taskId).returns(Future.successful(result))

  protected def mockRemoveTask(userId: UUID, taskId: UUID, result: Int) =
    (mockTodoService.removeTask _).expects(userId, taskId).returns(Future.successful(result))
}
