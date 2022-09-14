package me.anzop.todo

case class TodoPriorityDto(priority: Int)

object TodoPriorityDto {
  import me.anzop.todo.Validator._
  import spray.json.DefaultJsonProtocol._
  import spray.json.RootJsonFormat

  implicit val validator: Validator[TodoPriorityDto] = (dto: TodoPriorityDto) =>
    validateMinimum(dto.priority, 0, "priority")
      .map(TodoPriorityDto.apply)

  implicit val jsonFormat: RootJsonFormat[TodoPriorityDto] = jsonFormat1(TodoPriorityDto.apply)
}
