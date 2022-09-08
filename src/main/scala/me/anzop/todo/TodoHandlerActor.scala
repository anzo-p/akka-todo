package me.anzop.todo

import akka.actor.ActorLogging
import akka.persistence.{PersistentActor, SnapshotOffer}
import akka.util.Timeout

import scala.concurrent.duration.DurationInt

object TodoHandlerActor {
  sealed trait Command
  case object GetAllTodoLists extends Command
  case class AddTodoList(todo: TodoTaskParams) extends Command
  case class UpdateTodoList(todoId: String, todo: TodoTaskParams) extends Command

  sealed trait Event
  case class TodoListAdded(todo: TodoTask) extends Event
  case class TodoListUpdated(todo: TodoTask) extends Event
  case object Shutdown
}

class TodoHandlerActor(userId: String) extends PersistentActor with ActorLogging with SnapShooter {
  import TodoHandlerActor._

  implicit val timeout: Timeout = 10 seconds

  override def snapshotInterval: Int = 10

  override def persistenceId: String = s"todo-actor-$userId"

  var state: Map[String, TodoTask] = Map()

  override def receiveCommand: Receive = {
    case GetAllTodoLists =>
      sender() ! state.values

    case AddTodoList(params) =>
      val todo = TodoTask(params).copy(userId = userId)
      persist(TodoListAdded(todo)) { _ =>
        state += (todo.todoTaskId -> todo)
        sender() ! state.values
        if (maybeSnapshot) {
          saveSnapshot(state)
        }
      }

    case UpdateTodoList(id, params) =>
      state.get(id) match {
        case Some(curTodo) =>
          val todo = TodoTask(curTodo, params)
          persist(TodoListUpdated(todo)) { _ =>
            state += (todo.todoTaskId -> todo)
            sender() ! state.values
            if (maybeSnapshot) {
              saveSnapshot(state)
            }
          }

        case _ =>
      }

    case Shutdown =>
      context.stop(self)
  }

  override def receiveRecover: Receive = {
    case SnapshotOffer(_, payload: Map[String, TodoTask]) =>
      state = payload

    case TodoListAdded(todo) =>
      state += (todo.todoTaskId -> todo)

    case TodoListUpdated(todo) =>
      state += (todo.todoTaskId -> todo)
  }
}
