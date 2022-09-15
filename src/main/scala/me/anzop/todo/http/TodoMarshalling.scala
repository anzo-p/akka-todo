package me.anzop.todo.http

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

trait TodoMarshalling extends SprayJsonSupport with DefaultJsonProtocol
