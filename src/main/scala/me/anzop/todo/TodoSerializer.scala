package me.anzop.todo

import akka.serialization.SerializerWithStringManifest
import me.anzop.todo.TodoHandlerActor.TodoActorState
import me.anzop.todo.todoProtocol.{TodoActorStateProto, TodoTaskProto}
import scalapb.GeneratedMessage

class TodoSerializer extends SerializerWithStringManifest {
  import TodoSerializer._

  override def identifier: Int = 12345

  override def manifest(o: AnyRef): String = o match {
    case _: TodoActorStateProto => TodoTaskSnapShotManifest
    case _: TodoTaskProto       => TodoTaskEventManifest
  }

  override def toBinary(o: AnyRef): Array[Byte] = o.asInstanceOf[GeneratedMessage].toByteArray

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef =
    manifest match {
      case TodoTaskSnapShotManifest => TodoActorStateProto.parseFrom(bytes)
      case TodoTaskEventManifest    => TodoTaskProto.parseFrom(bytes)
    }
}

object TodoSerializer {
  final val TodoTaskSnapShotManifest = classOf[TodoActorStateProto].getName
  final val TodoTaskEventManifest    = classOf[TodoTaskProto].getName

  def toProto(todo: TodoTask): TodoTaskProto =
    TodoTaskProto(
      userId    = todo.userId,
      taskId    = todo.taskId,
      title     = todo.title,
      completed = todo.completed,
      priority  = todo.priority
    )

  def toProto(state: TodoActorState): TodoActorStateProto =
    TodoActorStateProto.of(state.transform { (_, v) =>
      TodoTaskProto.of(
        userId    = v.userId,
        taskId    = v.taskId,
        title     = v.title,
        priority  = v.priority,
        completed = v.completed
      )
    })

  def fromProto(proto: TodoActorStateProto): TodoActorState =
    proto.state.transform { (_, v) =>
      TodoTask(
        v.userId,
        v.taskId,
        v.title,
        v.priority,
        v.completed
      )
    }

  def fromProto(proto: TodoTaskProto): TodoTask =
    TodoTask(
      userId    = proto.userId,
      taskId    = proto.taskId,
      title     = proto.title,
      completed = proto.completed,
      priority  = proto.priority
    )
}
