package me.anzop.todo.http.dto

case class TaskIdDto(taskId: String)

object TaskIdDto {
  import me.anzop.todo.http.validation.Validation._

  implicit val validator: Validator[TaskIdDto] = (dto: TaskIdDto) =>
    validateUUID(dto.taskId, "taskId")
      .map(TaskIdDto.apply)
}
