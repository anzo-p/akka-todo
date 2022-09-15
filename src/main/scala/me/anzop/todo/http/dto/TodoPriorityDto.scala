package me.anzop.todo.http.dto

case class TodoPriorityDto(priority: Int)

object TodoPriorityDto {
  import me.anzop.todo.http.validation.Validation._
  import spray.json.DefaultJsonProtocol._
  import spray.json.RootJsonFormat

  implicit val jsonFormat: RootJsonFormat[TodoPriorityDto] = jsonFormat1(TodoPriorityDto.apply)

  implicit val validator: Validator[TodoPriorityDto] = (dto: TodoPriorityDto) =>
    validateMinimum(dto.priority, 0, "priority")
      .map(TodoPriorityDto.apply)
}
