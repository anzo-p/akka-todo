package me.anzop.todo.utils

import org.scalatest.wordspec.AnyWordSpecLike

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must
import org.scalatest.{EitherValues, Inspectors, OptionValues}

trait BaseSpec
    extends AnyWordSpecLike
    with must.Matchers
    with OptionValues
    with EitherValues
    with Inspectors
    with ScalaFutures
