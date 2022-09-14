package me.anzop.todo

case class TodoPriorityDto(priority: Int)

object TodoPriorityDto {
  import spray.json.DefaultJsonProtocol._
  import spray.json.RootJsonFormat

  implicit val jsonFormat: RootJsonFormat[TodoPriorityDto] = jsonFormat1(TodoPriorityDto.apply)
}
