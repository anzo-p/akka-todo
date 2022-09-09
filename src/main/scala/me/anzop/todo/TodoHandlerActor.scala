package me.anzop.todo

import akka.actor.ActorLogging
import akka.persistence.{PersistentActor, SaveSnapshotFailure, SaveSnapshotSuccess, SnapshotOffer}
import akka.util.Timeout
import todo.Todoserdes.{InMemoryTodoTasksProto, TodoListAddedProto, TodoTaskProto}

import java.util
import scala.concurrent.duration.DurationInt

object TodoHandlerActor {
  sealed trait Command
  case object GetAllTodoLists extends Command
  case class AddTodoList(todo: TodoTaskParams) extends Command
  case class UpdateTodoList(todoId: String, todo: TodoTaskParams) extends Command

  sealed trait Event
  //case class TodoListAdded(todo: TodoTaskProto) extends Event
  //case class TodoListUpdated(todo: TodoTaskProto) extends Event
  case object Shutdown
}

class TodoHandlerActor(userId: String) extends PersistentActor with ActorLogging with SnapShooter {
  import TodoHandlerActor._

  implicit val timeout: Timeout = 10 seconds

  override def snapshotInterval: Int = 2

  override def persistenceId: String = s"todo-actor-$userId"

  def serialize(todo: TodoTask): TodoListAddedProto = {
    TodoListAddedProto
      .newBuilder()
      .setTodo(
        TodoTaskProto
          .newBuilder()
          .setUserId(todo.userId)
          .setTodoTaskId(todo.todoTaskId)
          .setTitle(todo.title)
          .setPriorityOrder(todo.priorityOrder)
          .setCompleted(todo.completed)
      )
      .build()
  }

  def deserialize(todo: TodoListAddedProto): TodoTask = {
    val proto: TodoTaskProto = todo.getTodo
    TodoTask(
      proto.getUserId,
      proto.getTodoTaskId,
      proto.getTitle,
      proto.getPriorityOrder,
      proto.getCompleted
    )
  }

  def stateSer(state: Map[String, TodoTask]): InMemoryTodoTasksProto = {
    def f(task: TodoTask) = TodoTaskProto
      .newBuilder()
      .setUserId(task.userId)
      .setTodoTaskId(task.todoTaskId)
      .setTitle(task.title)
      .setPriorityOrder(task.priorityOrder)
      .setCompleted(task.completed)
      .build()

    var protoState = new java.util.HashMap[String, TodoTaskProto]
    state.foreach { entry =>
      protoState.put(entry._1, f(entry._2))
    }

    InMemoryTodoTasksProto
      .newBuilder()
      .putAllState(protoState)
      .build()
  }

  def stateDeser(state: InMemoryTodoTasksProto): Map[String, TodoTask] = {
    import scala.collection.JavaConverters._

    var loadState: Map[String, TodoTask] = Map()

    state.getStateMap.asScala.mapValues { entry =>
      val a = TodoTask(
        entry.getUserId,
        entry.getTodoTaskId,
        entry.getTitle,
        entry.getPriorityOrder,
        entry.getCompleted
      )
      loadState += a.todoTaskId -> a
    }
    loadState
  }

  var state: Map[String, TodoTask] = Map()

  override def receiveCommand: Receive = {
    case GetAllTodoLists =>
      sender() ! state.values

    case AddTodoList(params) =>
      val todo = TodoTask(params).copy(userId = userId)
      /*
        serialize
        received TodoTaskParams
        that were created into TodoTask
        into TodoListAddedProto

        somehow this seems overly complex
       */
      persist(serialize(todo)) { _ =>
        state += (todo.todoTaskId -> todo)
        sender() ! state.values
        if (maybeSnapshot) {
          log.info(s"snapshot due")
          saveSnapshot(stateSer(state))
        }
      }

    case UpdateTodoList(id, params) =>
      state.get(id) match {
        case Some(curTodo) =>
          val todo = TodoTask(curTodo, params)
          persist(serialize(todo)) { _ =>
            state += (todo.todoTaskId -> todo)
            sender() ! state.values
            if (maybeSnapshot) {
              saveSnapshot(state)
            }
          }

        case _ =>
      }

    case SaveSnapshotSuccess(metadata) =>
      log.info("snapshot successful move on")

    case SaveSnapshotFailure(metadata, cause) =>
      log.info("snapshot failed but we still move on")

    case Shutdown =>
      context.stop(self)
  }

  override def receiveRecover: Receive = {
    case SnapshotOffer(_, payload) =>
      state = stateDeser(payload.asInstanceOf[InMemoryTodoTasksProto])

    case event: TodoListAddedProto =>
      val todo = deserialize(event)
      state += todo.todoTaskId -> todo

    //case TodoListUpdated(todoProto) =>
    //  val todo = deserialize(todoProto)
    //  state += (todo.todoTaskId -> todo)
  }
}
