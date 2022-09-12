package me.anzop.todo

import akka.actor.ActorLogging
import akka.persistence.{PersistentActor, SaveSnapshotFailure, SaveSnapshotSuccess, SnapshotOffer}
import akka.util.Timeout
import me.anzop.todo.TodoSerializer.{fromProto, toProto}
import me.anzop.todo.todoProtocol.{TodoActorStateProto, TodoTaskProto}

import scala.concurrent.duration.DurationInt

object TodoHandlerActor {
  sealed trait Command
  case object GetAllTodoTasks extends Command
  case class AddTodoTask(todo: TodoTaskParams) extends Command
  case class UpdateTodoTask(taskId: String, todo: TodoTaskParams) extends Command
  case object Shutdown

  type TodoActorState = Map[String, TodoTask]
}

class TodoHandlerActor(userId: String) extends PersistentActor with ActorLogging with SnapShooter {
  import TodoHandlerActor._

  implicit val timeout: Timeout = 10 seconds

  override def snapshotInterval: Int = 10

  override def persistenceId: String = s"todo-actor-$userId"

  var state: TodoActorState = Map()

  override def receiveCommand: Receive = {
    case GetAllTodoTasks =>
      sender() ! state.values

    case AddTodoTask(params) =>
      val todo = TodoTask(params).copy(userId = userId)
      persist(toProto(todo)) { _ =>
        state += (todo.taskId -> todo)
        sender() ! state.values
        if (maybeSnapshot) {
          saveSnapshot(toProto(state))
        }
      }

    case SaveSnapshotSuccess(metadata) =>
    case SaveSnapshotFailure(_, reason) =>
      log.warning(s"Save snapshot failed on: $reason")

    case Shutdown =>
      context.stop(self)
  }

  override def receiveRecover: Receive = {
    case SnapshotOffer(_, snapshot: TodoActorStateProto) =>
      state = fromProto(snapshot)
      log.info(s"from snapshots alles ok")

    case data: TodoTaskProto =>
      val todo = fromProto(data)
      state += (todo.taskId -> todo)
      log.info(s"from replay events alles ok")
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
