package me.anzop.todo

import akka.serialization.SerializerWithStringManifest
import me.anzop.todo.TodoHandlerActor.TodoActorState
import me.anzop.todo.todoProtocol.{TodoActorStateProto, TodoTaskProto, TodoTaskSetPriorityProto}
import scalapb.GeneratedMessage

class TodoSerializer extends SerializerWithStringManifest {
  import TodoSerializer._

  override def identifier: Int = 12345

  override def manifest(o: AnyRef): String = o match {
    case _: TodoActorStateProto      => TodoTaskSnapShotManifest
    case _: TodoTaskProto            => TodoTaskAddManifest
    case _: TodoTaskSetPriorityProto => TodoTaskUpdatePriorityManifest
  }

  override def toBinary(o: AnyRef): Array[Byte] = o.asInstanceOf[GeneratedMessage].toByteArray

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef =
    manifest match {
      case TodoTaskSnapShotManifest       => TodoActorStateProto.parseFrom(bytes)
      case TodoTaskAddManifest            => TodoTaskProto.parseFrom(bytes)
      case TodoTaskUpdatePriorityManifest => TodoTaskSetPriorityProto.parseFrom(bytes)
    }
}

object TodoSerializer {
  final val TodoTaskSnapShotManifest       = classOf[TodoActorStateProto].getName
  final val TodoTaskAddManifest            = classOf[TodoTaskProto].getName
  final val TodoTaskUpdatePriorityManifest = classOf[TodoTaskSetPriorityProto].getName

  def toProtobuf(todo: TodoTask): TodoTaskProto =
    TodoTaskProto(
      userId    = todo.userId,
      taskId    = todo.taskId,
      title     = todo.title,
      completed = todo.completed,
      priority  = todo.priority
    )

  def toProtobuf(state: TodoActorState): TodoActorStateProto =
    TodoActorStateProto.of(state.transform { (_, v) =>
      TodoTaskProto.of(
        userId    = v.userId,
        taskId    = v.taskId,
        title     = v.title,
        priority  = v.priority,
        completed = v.completed
      )
    })

  def fromProtobuf(proto: TodoActorStateProto): TodoActorState =
    proto.state.transform { (_, v) =>
      TodoTask(
        v.userId,
        v.taskId,
        v.title,
        v.priority,
        v.completed
      )
    }

  def fromProtobuf(proto: TodoTaskProto): TodoTask =
    TodoTask(
      userId    = proto.userId,
      taskId    = proto.taskId,
      title     = proto.title,
      completed = proto.completed,
      priority  = proto.priority
    )
}
