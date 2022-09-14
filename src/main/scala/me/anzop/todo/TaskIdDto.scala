package me.anzop.todo

case class TaskIdDto(taskId: String)

object TaskIdDto {
  import me.anzop.todo.Validator._

  implicit val validator: Validator[TaskIdDto] = (dto: TaskIdDto) =>
    validateUUID(dto.taskId, "taskId")
      .map(TaskIdDto.apply)
}
