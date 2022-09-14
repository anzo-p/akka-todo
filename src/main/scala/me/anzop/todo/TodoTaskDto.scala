package me.anzop.todo

import java.util.UUID

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
  import cats.implicits._
  import me.anzop.todo.Validator._
  import spray.json.DefaultJsonProtocol._
  import spray.json.RootJsonFormat

  def fromModel(model: TodoTask): TodoTaskDto =
    TodoTaskDto(
      userId    = model.userId,
      taskId    = Some(model.taskId),
      title     = model.title,
      completed = Some(model.completed),
      priority  = Some(model.priority)
    )

  implicit val validator: Validator[TodoTaskDto] = (dto: TodoTaskDto) => {
    (
      accept(dto.userId),
      accept(Some(UUID.randomUUID().toString)),
      validateRequired(dto.title, "title"),
      accept(dto.completed),
      validateMinimum(dto.priority, 0, "priority")
    ).mapN(TodoTaskDto.apply)
  }

  implicit val jsonFormat: RootJsonFormat[TodoTaskDto] = jsonFormat5(TodoTaskDto.apply)
}
