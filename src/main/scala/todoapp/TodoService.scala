package todoapp

import akka.pattern.ask
import akka.util.Timeout
import todoapp.actor.TodoHandlerActor
import todoapp.models.{TodoTask, TodoTaskParams}

import java.util.UUID
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

trait TodoService extends TodoHandlerProvider {

  implicit val timeout: Timeout = 2 seconds

  def getAllTodos(user: UUID): Future[Iterable[TodoTask]] =
    (todoHandler(user) ? TodoHandlerActor.GetAllTodoTasks).mapTo[Iterable[TodoTask]]

  def getAllTodosByTitle(user: UUID, title: String): Future[Iterable[TodoTask]] =
    (todoHandler(user) ? TodoHandlerActor.GetTodoTasksByTitle(title)).mapTo[Iterable[TodoTask]]

  def addTodo(user: UUID, params: TodoTaskParams): Future[TodoTask] = {
    val todo = TodoTask(user, params)
    (todoHandler(user) ? TodoHandlerActor.AddTodoTask(todo)).mapTo[TodoTask]
  }

  def updatePriority(userId: UUID, taskId: UUID, priority: Int): Future[Int] =
    (todoHandler(userId) ? TodoHandlerActor.UpdatePriority(taskId, priority)).mapTo[Int]

  def updateCompleted(userId: UUID, taskId: UUID): Future[Int] =
    (todoHandler(userId) ? TodoHandlerActor.UpdateCompleted(taskId)).mapTo[Int]

  def removeTask(userId: UUID, taskId: UUID): Future[Int] =
    (todoHandler(userId) ? TodoHandlerActor.RemoveTask(taskId)).mapTo[Int]
}
