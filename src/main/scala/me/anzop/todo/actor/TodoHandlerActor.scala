package me.anzop.todo.actor

import akka.actor.ActorLogging
import akka.persistence.{PersistentActor, SaveSnapshotFailure, SaveSnapshotSuccess, SnapshotOffer}
import akka.util.Timeout
import me.anzop.todo.actor.ProtobufConversions._
import me.anzop.todo.models.TodoTask
import me.anzop.todo.todoProtocol._

import scala.concurrent.duration.DurationInt

object TodoHandlerActor {
  sealed trait Command
  case object GetAllTodoTasks extends Command
  case class GetTodoTasksByTitle(querySting: String) extends Command
  case class GetTodoTaskById(taskId: String) extends Command
  case class AddTodoTask(todo: TodoTask) extends Command
  case class UpdatePriority(taskId: String, priority: Integer) extends Command
  case class UpdateCompleted(taskId: String) extends Command
  case class RemoveTask(taskId: String) extends Command
  case object Shutdown

  type TodoActorState = Map[String, TodoTask]
}

class TodoHandlerActor(userId: String) extends PersistentActor with ActorLogging with SnapShootTally {
  import TodoHandlerActor._

  implicit val timeout: Timeout = 3 seconds

  override def persistenceId: String = s"todo-actor-$userId"

  override def snapshotInterval: Int = 10

  protected var state: TodoActorState = Map()

  private def getTasks(excludeRemoved: Boolean = true): Iterable[TodoTask] = {
    if (excludeRemoved) {
      state.values.filterNot(_.removed)
    } else {
      state.values
    }
  }

  private def addReplaceTask(todo: TodoTask): Unit =
    state += todo.taskId -> todo

  private def setPriority(taskId: String, priority: Int): Int =
    state
      .get(taskId)
      .fold(0) { task =>
        addReplaceTask(task.copy(priority = priority))
        1
      }

  private def setCompleted(taskId: String): Int =
    state
      .get(taskId)
      .fold(0) { task =>
        addReplaceTask(task.copy(completed = true))
        1
      }

  private def setRemoved(taskId: String): Int =
    state
      .get(taskId)
      .fold(0) { task =>
        addReplaceTask(task.copy(removed = true))
        1
      }

  private def maybeSaveSnapshot(): Unit =
    if (maybeSnapshotDue) saveSnapshot(toProtobuf(state))

  override def receiveCommand: Receive = {
    case GetAllTodoTasks =>
      sender() ! getTasks()
        .toList
        .sortBy(_.priority)

    case GetTodoTasksByTitle(querySting) =>
      sender() ! getTasks()
        .filter(_.title contains querySting)
        .toList
        .sortBy(_.title)

    case GetTodoTaskById(taskId) =>
      sender() ! getTasks(excludeRemoved = false)
        .find(_.taskId == taskId)

    case AddTodoTask(todo) =>
      persist(toProtobuf(todo)) { _ =>
        addReplaceTask(todo)
        sender() ! todo
        maybeSaveSnapshot()
      }

    case UpdatePriority(taskId, priority) =>
      persist(TodoTaskSetPriorityProto(taskId, priority)) { _ =>
        sender() ! setPriority(taskId, priority)
        maybeSaveSnapshot()
      }

    case UpdateCompleted(taskId) =>
      persist(TodoTaskSetCompletedProto(taskId)) { _ =>
        sender() ! setCompleted(taskId)
        maybeSaveSnapshot()
      }

    case RemoveTask(taskId) =>
      persist(TodoTaskSetRemovedProto(taskId)) { _ =>
        sender() ! setRemoved(taskId)
        maybeSaveSnapshot()
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
      log.info(s"receiveRecover SnapshotOffer")

    case data: TodoTaskProto =>
      addReplaceTask(fromProtobuf(data))
      log.info(s"receiveRecover TodoTaskProto")

    case data: TodoTaskSetPriorityProto =>
      setPriority(data.taskId, data.newPriority)

    case data: TodoTaskSetCompletedProto =>
      setCompleted(data.taskId)

    case data: TodoTaskSetRemovedProto =>
      setRemoved(data.taskId)
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
