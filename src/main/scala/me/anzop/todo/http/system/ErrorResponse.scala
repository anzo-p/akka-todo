package me.anzop.todo.http.system

case class ErrorResponse(reason: List[String])

object ErrorResponse {
  import spray.json.DefaultJsonProtocol._
  import spray.json.RootJsonFormat

  implicit val jsonFormat: RootJsonFormat[ErrorResponse] = jsonFormat1(ErrorResponse.apply)
}
