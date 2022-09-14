package me.anzop.todo

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route
import cats.data.Validated.{Invalid, Valid}
import me.anzop.todo.Validator._

trait BaseRoutes extends TodoMarshalling {

  def validateRequest[R : Validator](payload: R)(routeWhenValid: Route): Route =
    validateInput(payload) match {
      case Valid(_) =>
        routeWhenValid
      case Invalid(failures) =>
        complete(StatusCodes.BadRequest, ErrorResponse(failures.toList.map(_.errorMessage)))
    }
}
