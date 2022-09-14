package me.anzop.todo

sealed trait ValidationFailure {
  def errorMessage: String
}

final case class EmptyField(fieldName: String) extends ValidationFailure {
  override def errorMessage: String = s"'$fieldName' cannot be empty"
}

final case class NonUUID(fieldName: String) extends ValidationFailure {
  override def errorMessage: String = s"'$fieldName' must be in UUID format"
}

final case class NegativeValue(fieldName: String) extends ValidationFailure {
  override def errorMessage: String = s"'$fieldName' cannot be negative"
}
