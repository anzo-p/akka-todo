package todoapp.models

import java.util.UUID

case class TodoTask(
    userId: UUID,
    taskId: UUID,
    title: String,
    priority: Int,
    completed: Boolean,
    removed: Boolean = false
  )

object TodoTask {

  def apply(userId: UUID, params: TodoTaskParams): TodoTask =
    TodoTask(
      userId    = userId,
      taskId    = UUID.randomUUID(),
      title     = params.title,
      priority  = params.priority.getOrElse(0),
      completed = params.completed.getOrElse(false)
    )

  def apply(existingTask: TodoTask, params: TodoTaskParams): TodoTask =
    TodoTask(
      userId    = existingTask.userId,
      taskId    = existingTask.taskId,
      title     = params.title,
      priority  = params.priority.getOrElse(existingTask.priority),
      completed = params.completed.getOrElse(existingTask.completed),
      removed   = existingTask.removed
    )
}
