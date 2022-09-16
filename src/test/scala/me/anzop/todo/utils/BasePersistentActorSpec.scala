package me.anzop.todo.utils

import akka.actor.ActorSystem
import akka.persistence.inmemory.extension.{InMemoryJournalStorage, InMemorySnapshotStorage, StorageExtension}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}

class BasePersistentActorSpec
    extends TestKit(ActorSystem("TestActorSystem", ConfigFactory.load().getConfig("test")))
    with BaseSpec
    with ImplicitSender
    with BeforeAndAfterEach
    with BeforeAndAfterAll {

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  override protected def beforeEach(): Unit = {
    val probe = TestProbe()
    probe.send(StorageExtension(system).journalStorage, InMemoryJournalStorage.ClearJournal)
    probe.expectMsg(akka.actor.Status.Success(""))
    probe.send(StorageExtension(system).snapshotStorage, InMemorySnapshotStorage.ClearSnapshots)
    probe.expectMsg(akka.actor.Status.Success(""))
    super.beforeEach()
  }
}
