package me.anzop.todo

import akka.serialization.SerializerWithStringManifest
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
      userId        = todo.userId,
      todoTaskId    = todo.todoTaskId,
      title         = todo.title,
      completed     = todo.completed,
      priorityOrder = todo.priorityOrder
    )

  def toProto(state: Map[String, TodoTask]): TodoActorStateProto =
    TodoActorStateProto.of(state.transform { (_, v) =>
      TodoTaskProto.of(
        userId        = v.userId,
        todoTaskId    = v.todoTaskId,
        title         = v.title,
        priorityOrder = v.priorityOrder,
        completed     = v.completed
      )
    })

  def fromProto(proto: TodoActorStateProto): Map[String, TodoTask] =
    proto.state.transform { (_, v) =>
      TodoTask(
        v.userId,
        v.todoTaskId,
        v.title,
        v.priorityOrder,
        v.completed
      )
    }

  def fromProto(proto: TodoTaskProto): TodoTask =
    TodoTask(
      userId        = proto.userId,
      todoTaskId    = proto.todoTaskId,
      title         = proto.title,
      completed     = proto.completed,
      priorityOrder = proto.priorityOrder
    )
}
