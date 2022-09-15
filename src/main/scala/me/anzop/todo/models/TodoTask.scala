package me.anzop.todo.models

import java.util.UUID

case class TodoTask(
    userId: String,
    taskId: String,
    title: String      = "",
    priority: Int      = 0,
    completed: Boolean = false,
    removed: Boolean   = false
  )

object TodoTask {

  def apply(params: TodoTaskParams): TodoTask =
    TodoTask(
      userId = params.userId,
      taskId = UUID.randomUUID().toString,
      title  = params.title
    )

  def apply(item: TodoTask, params: TodoTaskParams): TodoTask =
    TodoTask(
      userId    = item.userId,
      taskId    = item.taskId,
      title     = params.title,
      priority  = params.priority.getOrElse(item.priority),
      completed = params.completed.getOrElse(item.completed),
      removed   = params.completed.getOrElse(item.removed)
    )
}
