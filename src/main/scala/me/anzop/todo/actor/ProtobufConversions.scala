package me.anzop.todo.actor

import me.anzop.todo.actor.TodoHandlerActor.TodoActorState
import me.anzop.todo.models.TodoTask
import me.anzop.todo.todoProtocol.{TodoActorStateProto, TodoTaskProto}

import java.util.UUID

object ProtobufConversions {

  def toProtobuf(todo: TodoTask): TodoTaskProto =
    TodoTaskProto(
      todo.userId.toString,
      todo.taskId.toString,
      todo.title,
      todo.priority,
      todo.completed,
      todo.removed
    )

  def toProtobuf(state: TodoActorState): TodoActorStateProto = {
    TodoActorStateProto.of(state.map {
      case (k, v) =>
        k.toString ->
          TodoTaskProto.of(
            v.userId.toString,
            v.taskId.toString,
            v.title,
            v.priority,
            v.completed,
            v.removed
          )
    })
  }

  def fromProtobuf(proto: TodoActorStateProto): TodoActorState = {
    proto.state.map {
      case (k, v) =>
        UUID.fromString(k) ->
          TodoTask(
            UUID.fromString(v.userId),
            UUID.fromString(v.taskId),
            v.title,
            v.priority,
            v.completed,
            v.removed
          )
    }
  }

  def fromProtobuf(proto: TodoTaskProto): TodoTask =
    TodoTask(
      UUID.fromString(proto.userId),
      UUID.fromString(proto.taskId),
      proto.title,
      proto.priority,
      proto.completed,
      proto.removed
    )
}
