package me.anzop.todo

import akka.pattern.ask
import akka.util.Timeout
import me.anzop.todo.actor.TodoHandlerActor
import me.anzop.todo.models.{TodoTask, TodoTaskParams}

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

trait TodoService extends TodoHandlerProvider {

  implicit val timeout: Timeout = 2 seconds

  protected def getAllTodos(user: String, title: String): Future[Iterable[TodoTask]] =
    (todoHandler(user) ? TodoHandlerActor.GetTodoTasksByTitle(title)).mapTo[Iterable[TodoTask]]

  protected def getTodo(user: String): Future[Iterable[TodoTask]] =
    (todoHandler(user) ? TodoHandlerActor.GetAllTodoTasks).mapTo[Iterable[TodoTask]]

  protected def addTodo(user: String, payload: TodoTaskParams): Future[TodoTask] = {
    val todo = TodoTask(payload.copy(userId = user))
    (todoHandler(user) ? TodoHandlerActor.AddTodoTask(todo)).mapTo[TodoTask]
  }

  protected def updatePriority(userId: String, taskId: String, priority: Int): Future[Int] =
    (todoHandler(userId) ? TodoHandlerActor.UpdatePriority(taskId, priority)).mapTo[Int]

  protected def updateCompleted(userId: String, taskId: String): Future[Int] =
    (todoHandler(userId) ? TodoHandlerActor.UpdateCompleted(taskId)).mapTo[Int]

  protected def removeTask(userId: String, taskId: String): Future[Int] =
    (todoHandler(userId) ? TodoHandlerActor.RemoveTask(taskId)).mapTo[Int]
}
