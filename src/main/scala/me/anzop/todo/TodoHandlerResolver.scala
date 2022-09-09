package me.anzop.todo

import akka.actor.{ActorRef, ActorSystem, Cancellable, Props}

import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConverters.enumerationAsScalaIteratorConverter
import scala.concurrent.duration.{DurationInt, FiniteDuration}

trait TodoHandlerResolver {
  implicit val system: ActorSystem

  private case class ActorTimeout(actor: ActorRef, timeout: Cancellable)

  private val actorLifetime: FiniteDuration                         = 10 seconds
  private val activeActors: ConcurrentHashMap[String, ActorTimeout] = new ConcurrentHashMap[String, ActorTimeout]()

  private def passivate(actorName: String, ref: ActorRef): Runnable = () => {
    ref ! TodoHandlerActor.Shutdown
    activeActors.remove(actorName)
    system.log.info(s"passivating actor $actorName, state now: $activeActors")
  }

  private def scheduleTimeout(actorName: String, ref: ActorRef): Cancellable =
    system.scheduler.scheduleOnce(actorLifetime, passivate(actorName, ref))(system.dispatcher)

  private def composeTimeout(actorName: String, ref: ActorRef): ActorTimeout =
    ActorTimeout(ref, scheduleTimeout(actorName, ref))

  def todoHandler(selector: String): ActorRef = {
    val wouldBeActor = s"todo-handler-$selector"

    if (activeActors.keys().asScala.toSet.contains(wouldBeActor)) {
      activeActors.get(wouldBeActor).timeout.cancel()
      val existingActor = activeActors.get(wouldBeActor).actor
      activeActors.put(wouldBeActor, composeTimeout(wouldBeActor, existingActor))
      system.log.info(s"discovered existing actor ${activeActors.get(wouldBeActor)}")
      existingActor

    } else {
      val newActor = system.actorOf(Props(new TodoHandlerActor(selector)), wouldBeActor)
      activeActors.put(wouldBeActor, composeTimeout(wouldBeActor, newActor))
      system.log.info(s"created new actor $newActor")
      newActor
    }
  }
}
