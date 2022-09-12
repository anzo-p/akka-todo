package me.anzop.todo

import akka.actor.ActorLogging
import akka.persistence.{PersistentActor, SaveSnapshotFailure, SaveSnapshotSuccess, SnapshotOffer}
import akka.util.Timeout
import me.anzop.todo.TodoSerializer.{fromProtobuf, toProtobuf}
import me.anzop.todo.todoProtocol.{TodoActorStateProto, TodoTaskProto, TodoTaskSetPriorityProto}

import scala.concurrent.duration.DurationInt

object TodoHandlerActor {
  sealed trait Command
  case object GetAllTodoTasks extends Command
  case class GetTodoTasksByTitle(querySting: String) extends Command
  case class AddTodoTask(todo: TodoTaskParams) extends Command
  case class UpdatePriority(taskId: String, priority: Integer) extends Command
  case object Shutdown

  type TodoActorState = Map[String, TodoTask]
}

class TodoHandlerActor(userId: String) extends PersistentActor with ActorLogging with SnapShooter {
  import TodoHandlerActor._

  implicit val timeout: Timeout = 10 seconds

  override def snapshotInterval: Int = 10

  override def persistenceId: String = s"todo-actor-$userId"

  protected var state: TodoActorState = Map()

  private def addTask(todo: TodoTask): Unit =
    state += todo.taskId -> todo

  private def setPriority(taskId: String, priority: Int): Boolean =
    state.get(taskId) match {
      case None =>
        false
      case Some(task) =>
        state += taskId -> task.copy(priority = priority)
        true
    }

  private def sortByPriority(todos: Iterable[TodoTask]): Iterable[TodoTask] =
    todos.toList.sortBy(_.priority)

  private def sortByTitle(todos: Iterable[TodoTask]): Iterable[TodoTask] =
    todos.toList.sortBy(_.title)

  override def receiveCommand: Receive = {
    case GetAllTodoTasks =>
      sender() ! sortByPriority(state.values)

    case GetTodoTasksByTitle(querySting) =>
      sender() ! sortByTitle {
        state.values.filter(_.title contains querySting)
      }

    case AddTodoTask(params) =>
      val todo = TodoTask(params).copy(userId = userId)
      persist(toProtobuf(todo)) { _ =>
        addTask(todo)
        sender() ! todo
        if (maybeSnapshot) {
          saveSnapshot(toProtobuf(state))
        }
      }

    case UpdatePriority(taskId, priority) =>
      persist(TodoTaskSetPriorityProto(taskId, priority)) { _ =>
        sender() ! setPriority(taskId, priority)
        if (maybeSnapshot) {
          saveSnapshot(toProtobuf(state))
        }
      }

    case SaveSnapshotSuccess(_) =>
    case SaveSnapshotFailure(_, reason) =>
      log.warning(s"Save snapshot failed on: $reason")

    case Shutdown =>
      context.stop(self)
  }

  override def receiveRecover: Receive = {
    case SnapshotOffer(_, snapshot: TodoActorStateProto) =>
      state = fromProtobuf(snapshot)
      log.info(s"from snapshots alles ok")

    case data: TodoTaskProto =>
      addTask(fromProtobuf(data))
      log.info(s"from replay events alles ok")

    case data: TodoTaskSetPriorityProto =>
      setPriority(data.taskId, data.newPriority)
  }

  override def onPersistFailure(cause: Throwable, event: Any, seqNr: Long): Unit = {
    log.info(s"persistence failure because $cause")
    // restart actor - see supervising strategies with backoff
    super.onPersistFailure(cause, event, seqNr)
  }

  override def onPersistRejected(cause: Throwable, event: Any, seqNr: Long): Unit = {
    log.info(s"persistence rejected because $cause")
    super.onPersistRejected(cause, event, seqNr)
  }
}
