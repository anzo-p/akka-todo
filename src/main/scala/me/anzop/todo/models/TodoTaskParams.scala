package me.anzop.todo.models

case class TodoTaskParams(title: String, priority: Option[Int], completed: Option[Boolean])
