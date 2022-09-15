package me.anzop.todo.actor

import me.anzop.todo.actor.TodoHandlerActor.TodoActorState
import me.anzop.todo.models.TodoTask
import me.anzop.todo.todoProtocol.{TodoActorStateProto, TodoTaskProto}

object ProtobufConversions {

  def toProtobuf(todo: TodoTask): TodoTaskProto =
    TodoTaskProto(
      todo.userId,
      todo.taskId,
      todo.title,
      todo.priority,
      todo.completed,
      todo.removed
    )

  def toProtobuf(state: TodoActorState): TodoActorStateProto =
    TodoActorStateProto.of(state.transform { (_, v) =>
      TodoTaskProto.of(
        v.userId,
        v.taskId,
        v.title,
        v.priority,
        v.completed,
        v.removed
      )
    })

  def fromProtobuf(proto: TodoActorStateProto): TodoActorState =
    proto.state.transform { (_, v) =>
      TodoTask(
        v.userId,
        v.taskId,
        v.title,
        v.priority,
        v.completed,
        v.removed
      )
    }

  def fromProtobuf(proto: TodoTaskProto): TodoTask =
    TodoTask(
      proto.userId,
      proto.taskId,
      proto.title,
      proto.priority,
      proto.completed,
      proto.removed
    )
}
