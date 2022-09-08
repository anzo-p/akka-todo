package me.anzop.todo

import java.util.UUID

case class TodoTask(
    userId: String,
    todoTaskId: String,
    title: String      = "",
    priorityOrder: Int = 0,
    completed: Boolean = false
  )

object TodoTask {

  def apply(params: TodoTaskParams): TodoTask =
    TodoTask(
      userId     = params.userId,
      todoTaskId = UUID.randomUUID().toString,
      title      = params.title
    )

  def apply(item: TodoTask, params: TodoTaskParams): TodoTask =
    TodoTask(
      userId        = item.userId,
      todoTaskId    = item.todoTaskId,
      title         = params.title,
      priorityOrder = params.order.getOrElse(item.priorityOrder),
      completed     = params.completed.getOrElse(item.completed)
    )
}
