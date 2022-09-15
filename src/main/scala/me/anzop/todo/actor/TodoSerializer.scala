package me.anzop.todo.actor

import akka.serialization.SerializerWithStringManifest
import me.anzop.todo.todoProtocol._
import scalapb.GeneratedMessage

class TodoSerializer extends SerializerWithStringManifest {
  import TodoSerializer._

  override def identifier: Int = 12345

  override def manifest(o: AnyRef): String = o match {
    case _: TodoActorStateProto       => TodoTaskSnapShotManifest
    case _: TodoTaskProto             => TodoTaskAddManifest
    case _: TodoTaskSetPriorityProto  => TodoTaskSetPriorityManifest
    case _: TodoTaskSetCompletedProto => TodoTaskSetCompletedManifest
    case _: TodoTaskSetRemovedProto   => TodoTaskSetRemovedManifest
  }

  override def toBinary(o: AnyRef): Array[Byte] = o.asInstanceOf[GeneratedMessage].toByteArray

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef =
    manifest match {
      case TodoTaskSnapShotManifest     => TodoActorStateProto.parseFrom(bytes)
      case TodoTaskAddManifest          => TodoTaskProto.parseFrom(bytes)
      case TodoTaskSetPriorityManifest  => TodoTaskSetPriorityProto.parseFrom(bytes)
      case TodoTaskSetCompletedManifest => TodoTaskSetCompletedProto.parseFrom(bytes)
      case TodoTaskSetRemovedManifest   => TodoTaskSetRemovedProto.parseFrom(bytes)
    }
}

object TodoSerializer {
  final val TodoTaskSnapShotManifest     = classOf[TodoActorStateProto].getName
  final val TodoTaskAddManifest          = classOf[TodoTaskProto].getName
  final val TodoTaskSetPriorityManifest  = classOf[TodoTaskSetPriorityProto].getName
  final val TodoTaskSetCompletedManifest = classOf[TodoTaskSetCompletedProto].getName
  final val TodoTaskSetRemovedManifest   = classOf[TodoTaskSetRemovedProto].getName
}
