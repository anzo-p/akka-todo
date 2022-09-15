package me.anzop.todo.models

case class TodoTaskParams(
    userId: String,
    title: String,
    completed: Option[Boolean],
    priority: Option[Int]
  )
