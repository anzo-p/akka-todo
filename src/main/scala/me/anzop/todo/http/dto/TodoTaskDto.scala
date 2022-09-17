package me.anzop.todo.http.dto

import me.anzop.todo.models.{TodoTask, TodoTaskParams}

case class TodoTaskDto(
    userId: Option[String],
    taskId: Option[String],
    title: String,
    priority: Option[Int],
    completed: Option[Boolean]
  ) {

  def toParams: TodoTaskParams =
    TodoTaskParams(title, priority, completed)
}

object TodoTaskDto {
  import cats.implicits._
  import me.anzop.todo.http.validation.Validation._
  import spray.json.DefaultJsonProtocol._
  import spray.json.RootJsonFormat
  implicit val jsonFormat: RootJsonFormat[TodoTaskDto] = jsonFormat5(TodoTaskDto.apply)

  implicit val validator: Validator[TodoTaskDto] = (dto: TodoTaskDto) => {
    (
      accept(Some("")),
      accept(Some("")),
      validateRequired(dto.title, "title"),
      validateMinimum(dto.priority, 0, "priority"),
      accept(dto.completed)
    ).mapN(TodoTaskDto.apply)
  }

  def fromModel(model: TodoTask): TodoTaskDto =
    TodoTaskDto(
      userId    = Some(model.userId.toString),
      taskId    = Some(model.taskId.toString),
      title     = model.title,
      priority  = Some(model.priority),
      completed = Some(model.completed)
    )

  def toList(todos: Iterable[TodoTask]): Iterable[TodoTaskDto] =
    todos.map(todo => TodoTaskDto.fromModel(todo))
}
