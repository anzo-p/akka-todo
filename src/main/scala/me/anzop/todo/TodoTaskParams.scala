package me.anzop.todo

case class TodoTaskParams(
    userId: String,
    title: String,
    completed: Option[Boolean],
    priority: Option[Int]
  )
