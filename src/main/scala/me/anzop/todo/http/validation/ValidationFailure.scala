package me.anzop.todo.http.validation

sealed trait ValidationFailure {
  def errorMessage: String
}

final case class EmptyField(fieldName: String) extends ValidationFailure {
  override def errorMessage: String = s"'$fieldName' cannot be empty"
}

final case class NonUUIDField(fieldName: String) extends ValidationFailure {
  override def errorMessage: String = s"'$fieldName' must be in UUID format"
}

final case class NegativeValue(fieldName: String) extends ValidationFailure {
  override def errorMessage: String = s"'$fieldName' cannot be negative"
}

final case class BelowMinimumValue(fieldName: String, threshold: Double) extends ValidationFailure {
  override def errorMessage: String = s"'$fieldName' cannot be below minimum value ($threshold)"
}
