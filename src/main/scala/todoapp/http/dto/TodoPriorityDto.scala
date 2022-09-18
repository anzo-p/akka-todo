package todoapp.http.dto

case class TodoPriorityDto(priority: Int)

object TodoPriorityDto {
  import spray.json.DefaultJsonProtocol._
  import spray.json.RootJsonFormat
  import todoapp.http.validation.Validation._

  implicit val jsonFormat: RootJsonFormat[TodoPriorityDto] = jsonFormat1(TodoPriorityDto.apply)

  implicit val validator: Validator[TodoPriorityDto] = (dto: TodoPriorityDto) =>
    validateMinimum(dto.priority, 0, "priority")
      .map(TodoPriorityDto.apply)
}
