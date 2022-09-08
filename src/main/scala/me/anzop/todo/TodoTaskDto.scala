package me.anzop.todo

import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

case class TodoTaskDto(
    userId: String,
    itemId: Option[String],
    title: String,
    completed: Option[Boolean],
    priorityOrder: Option[Int]
  ) {

  def toParams: TodoTaskParams =
    TodoTaskParams(userId, title, completed, priorityOrder)
}

object TodoTaskDto {

  def fromModel(model: TodoTask): TodoTaskDto =
    TodoTaskDto(
      userId        = model.userId,
      itemId        = Some(model.todoTaskId),
      title         = model.title,
      completed     = Some(model.completed),
      priorityOrder = Some(model.priorityOrder)
    )

  implicit val jsonFormat: RootJsonFormat[TodoTaskDto] = jsonFormat5(TodoTaskDto.apply)
}
