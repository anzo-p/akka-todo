package me.anzop.todo.models

case class TodoTaskParams(
    userId: String,
    title: String,
    priority: Option[Int],
    completed: Option[Boolean]
  )
