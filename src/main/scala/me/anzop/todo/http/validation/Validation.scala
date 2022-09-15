package me.anzop.todo.http.validation

import cats.data.ValidatedNel
import cats.implicits.catsSyntaxValidatedId

trait ValidationAPI extends ValidationRules {
  type ValidationResult[A] = ValidatedNel[ValidationFailure, A]

  def accept[A](input: A): ValidationResult[A] =
    input.validNel

  def validateRequired[A : Required](input: A, fieldName: String): ValidationResult[A] =
    if (required(input)) {
      input.validNel
    } else {
      EmptyField(fieldName).invalidNel
    }

  def validateUUID[A : UUIDRequired](input: A, fieldName: String): ValidationResult[A] =
    if (uuidRequired(input)) {
      input.validNel
    } else {
      NonUUID(fieldName).invalidNel
    }

  def validateMinimum[A : Minimum](input: A, threshold: Double, fieldName: String): ValidationResult[A] =
    minimum(input, threshold) match {
      case false if threshold == 0 => NegativeValue(fieldName).invalidNel
      case true                    => input.validNel
    }
}

object Validation extends ValidationAPI {

  trait Validator[A] {
    def validate(value: A): ValidationResult[A]
  }

  def validateInput[A](input: A)(implicit validator: Validator[A]): ValidationResult[A] = validator.validate(input)
}
