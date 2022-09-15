package me.anzop.todo.models

import java.util.UUID

case class TodoTask(
    userId: String,
    taskId: String,
    title: String,
    priority: Int,
    completed: Boolean,
    removed: Boolean = false
  )

object TodoTask {

  def apply(params: TodoTaskParams): TodoTask =
    TodoTask(
      userId    = params.userId,
      taskId    = UUID.randomUUID().toString,
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
