package todoapp.models

case class TodoTaskParams(title: String, priority: Option[Int], completed: Option[Boolean])
