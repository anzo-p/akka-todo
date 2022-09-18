package todoapp.system

import akka.actor.{ActorRef, ActorSystem, Cancellable}

import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConverters.enumerationAsScalaIteratorConverter
import scala.concurrent.duration.FiniteDuration

trait ActorResolver {
  implicit val system: ActorSystem

  protected case class ActorTimeout(actor: ActorRef, timeout: Cancellable)

  protected def actorLifetime: FiniteDuration

  protected def createNewActor(selector: String, actorName: String): ActorRef

  protected def shutDownActor(ref: ActorRef): Unit

  protected val activeActors: ConcurrentHashMap[String, ActorTimeout] = new ConcurrentHashMap[String, ActorTimeout]()

  private def passivate(actorName: String, ref: ActorRef): Runnable = () => {
    shutDownActor(ref)
    activeActors.remove(actorName)
    system.log.info(s"passivating actor $actorName, state now: $activeActors")
  }

  private def scheduleTimeout(actorName: String, ref: ActorRef): Cancellable =
    system.scheduler.scheduleOnce(actorLifetime, passivate(actorName, ref))(system.dispatcher)

  protected def composeTimeout(actorName: String, ref: ActorRef): ActorTimeout =
    ActorTimeout(ref, scheduleTimeout(actorName, ref))

  protected def resolveActor(selector: String, actorName: String): ActorRef = {
    if (activeActors.keys().asScala.toSet.contains(actorName)) {
      val existing = activeActors.get(actorName)
      existing.timeout.cancel()
      activeActors.put(actorName, composeTimeout(actorName, existing.actor))
      system.log.info(s"discovered existing actor ${activeActors.get(actorName)}")
      existing.actor

    } else {
      val newActor = createNewActor(selector, actorName)
      activeActors.put(actorName, composeTimeout(actorName, newActor))
      system.log.info(s"created new actor $newActor")
      newActor
    }
  }
}
