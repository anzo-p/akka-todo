package me.anzop.todo.http.validation

import cats.implicits.catsSyntaxValidatedId
import me.anzop.todo.actor.ArbitraryTasks.{sample, PositiveInteger, UUIDString}
import me.anzop.todo.http.validation.Validation._
import me.anzop.todo.utils.BaseSpec

class ValidationSpec extends BaseSpec {

  private val fieldName = sample[String]

  "validateRequired" should {
    "return valid for a non-empty string" in {
      val label  = sample[String]
      val result = validateRequired(label, fieldName)
      result mustBe label.validNel
    }
    "return invalid for an empty string" in {
      val result = validateRequired("", fieldName)
      result mustBe EmptyField(fieldName).invalidNel
    }
  }

  "validateUUID" should {
    "return valid for a proper UUID string" in {
      val label  = sample[UUIDString].value
      val result = validateUUID(label, fieldName)
      result mustBe label.validNel
    }
    "return invalid for an empty string" in {
      val result = validateUUID("", fieldName)
      result mustBe NonUUIDField(fieldName).invalidNel
    }
    "return invalid for a non-UUID string" in {
      val label  = sample[String]
      val result = validateUUID(label, fieldName)
      result mustBe NonUUIDField(fieldName).invalidNel
    }
    "return valid for an option of a proper UUID string" in {
      val label  = sample[UUIDString].value
      val result = validateUUID(Option(label), fieldName)
      result mustBe Some(label).validNel
    }
    "return invalid for an option of an empty string" in {
      val result = validateUUID(Option(""), fieldName)
      result mustBe NonUUIDField(fieldName).invalidNel
    }
    "return invalid for an option of a non-UUID string" in {
      val label  = sample[String]
      val result = validateUUID(Option(label), fieldName)
      result mustBe NonUUIDField(fieldName).invalidNel
    }
  }

  "validateMinimum" should {

    "BelowMinimumValue" when {
      "return valid for an integer that exceeds a negative minimum" in {
        val value  = sample[PositiveInteger].value * -1
        val result = validateMinimum(value, value - 1, fieldName)
        result mustBe value.validNel
      }
      "return valid for an integer that is at a negative minimum" in {
        val value  = sample[PositiveInteger].value * -1
        val result = validateMinimum(value, value, fieldName)
        result mustBe value.validNel
      }
      "return invalid for an integer that falls short of a negative minimum" in {
        val value   = sample[PositiveInteger].value * -1
        val minimum = value + 1
        val result  = validateMinimum(value, minimum, fieldName)
        result mustBe BelowMinimumValue(fieldName, minimum).invalidNel
      }

      "return valid for an integer that exceeds a positive minimum" in {
        val value  = sample[PositiveInteger].value
        val result = validateMinimum(value, 0, fieldName)
        result mustBe value.validNel
      }
      "return valid for an integer that is at a positive minimum" in {
        val value  = sample[PositiveInteger].value
        val result = validateMinimum(value, value, fieldName)
        result mustBe value.validNel
      }
      "return invalid for an integer that falls short of a positive minimum" in {
        val value   = sample[PositiveInteger].value
        val minimum = value + 1
        val result  = validateMinimum(value, minimum, fieldName)
        result mustBe BelowMinimumValue(fieldName, minimum).invalidNel
      }
    }

    "Option BelowMinimumValue" when {
      "return valid for an integer that exceeds a negative minimum" in {
        val value  = sample[PositiveInteger].value * -1
        val result = validateMinimum(Option(value), value - 1, fieldName)
        result mustBe Some(value).validNel
      }
      "return valid for an integer that is at a negative minimum" in {
        val value  = sample[PositiveInteger].value * -1
        val result = validateMinimum(Option(value), value, fieldName)
        result mustBe Some(value).validNel
      }
      "return invalid for an integer that falls short of a negative minimum" in {
        val value   = sample[PositiveInteger].value * -1
        val minimum = value + 1
        val result  = validateMinimum(Option(value), minimum, fieldName)
        result mustBe BelowMinimumValue(fieldName, minimum).invalidNel
      }

      "return valid for an integer that exceeds a positive minimum" in {
        val value  = sample[PositiveInteger].value
        val result = validateMinimum(Option(value), 0, fieldName)
        result mustBe Some(value).validNel
      }
      "return valid for an integer that is at a positive minimum" in {
        val value  = sample[PositiveInteger].value
        val result = validateMinimum(Option(value), value, fieldName)
        result mustBe Some(value).validNel
      }
      "return invalid for an integer that falls short of a positive minimum" in {
        val value   = sample[PositiveInteger].value
        val minimum = value + 1
        val result  = validateMinimum(Option(value), minimum, fieldName)
        result mustBe BelowMinimumValue(fieldName, minimum).invalidNel
      }
    }

    "NegativeValue" when {
      "return valid for an integer that exceeds a minimum of zero" in {
        val value  = sample[PositiveInteger].value + 1
        val result = validateMinimum(value, 0, fieldName)
        result mustBe value.validNel
      }
      "return valid for an integer that is at a minimum of zero" in {
        val value  = 0
        val result = validateMinimum(value, 0, fieldName)
        result mustBe value.validNel
      }
      "return invalid for an integer that falls short of a minimum of zero" in {
        val value  = sample[PositiveInteger].value * -1 - 1
        val result = validateMinimum(value, 0, fieldName)
        result mustBe NegativeValue(fieldName).invalidNel
      }
    }

    "Option NegativeValue" when {
      "return valid for an integer that exceeds a minimum of zero" in {
        val value  = sample[PositiveInteger].value + 1
        val result = validateMinimum(Option(value), 0, fieldName)
        result mustBe Some(value).validNel
      }
      "return valid for an integer that is at a minimum of zero" in {
        val value  = 0
        val result = validateMinimum(Option(value), 0, fieldName)
        result mustBe Some(value).validNel
      }
      "return invalid for an integer that falls short of a minimum of zero" in {
        val value  = sample[PositiveInteger].value * -1 - 1
        val result = validateMinimum(Option(value), 0, fieldName)
        result mustBe NegativeValue(fieldName).invalidNel
      }
    }
  }
}
