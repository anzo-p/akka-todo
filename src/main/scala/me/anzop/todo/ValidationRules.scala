package me.anzop.todo

object ValidationRules {
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
}
