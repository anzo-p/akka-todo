package me.anzop.todo

import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

case class TodoTaskDto(
    userId: String,
    taskId: Option[String],
    title: String,
    completed: Option[Boolean],
    priority: Option[Int]
  ) {

  def toParams: TodoTaskParams =
    TodoTaskParams(userId, title, completed, priority)
}

object TodoTaskDto {

  def fromModel(model: TodoTask): TodoTaskDto =
    TodoTaskDto(
      userId    = model.userId,
      taskId    = Some(model.taskId),
      title     = model.title,
      completed = Some(model.completed),
      priority  = Some(model.priority)
    )

  implicit val jsonFormat: RootJsonFormat[TodoTaskDto] = jsonFormat5(TodoTaskDto.apply)
}
