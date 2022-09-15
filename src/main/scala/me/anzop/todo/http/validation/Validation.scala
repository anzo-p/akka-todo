package me.anzop.todo.http.validation

import cats.data.ValidatedNel
import cats.implicits.catsSyntaxValidatedId

object Validation {
  trait Required[A] extends (A => Boolean)

  trait UUIDRequired[A] extends (A => Boolean)

  trait Minimum[A] extends ((A, Double) => Boolean)

  implicit val requiredString: Required[String] = _.nonEmpty

  implicit val uuidRequired: UUIDRequired[String] =
    _.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")

  implicit val requiredOptionUUID: UUIDRequired[Option[String]] =
    _.fold(true) { v =>
      v.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")
    }

  implicit val minimumInt: Minimum[Int] = _ >= _

  implicit val minimumOptionInt: Minimum[Option[Int]] = (a, b) => a.fold(true) { _ >= b }

  def required[A](value: A)(implicit req: Required[A]): Boolean = req(value)

  def uuidRequired[A](value: A)(implicit req: UUIDRequired[A]): Boolean = req(value)

  def minimum[A](value: A, threshold: Double)(implicit min: Minimum[A]): Boolean = min(value, threshold)

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
      NonUUIDField(fieldName).invalidNel
    }

  def validateMinimum[A : Minimum](input: A, threshold: Double, fieldName: String): ValidationResult[A] =
    minimum(input, threshold) match {
      case false if threshold == 0 => NegativeValue(fieldName).invalidNel
      case false                   => BelowMinimumValue(fieldName, threshold).invalidNel
      case true                    => input.validNel
    }

  trait Validator[A] {
    def validate(value: A): ValidationResult[A]
  }

  def validateInput[A](input: A)(implicit validator: Validator[A]): ValidationResult[A] = validator.validate(input)
}
