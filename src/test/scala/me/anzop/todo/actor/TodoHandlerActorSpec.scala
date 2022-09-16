package me.anzop.todo.actor

import akka.actor.Props
import me.anzop.todo.actor.ArbitraryTasks.{sample, PositiveInteger, SeveralTodoTasks, UUIDString}
import me.anzop.todo.actor.TodoHandlerActor._
import me.anzop.todo.utils.BasePersistentActorSpec

class TodoHandlerActorSpec extends BasePersistentActorSpec {

  private val setup  = sample[SeveralTodoTasks]
  private val userId = setup.tasks.head.userId
  private val tasks  = setup.tasks

  private val allTasksAreActive  = tasks.map(_.copy(completed = false, removed = false))
  private val allTasksAreRemoved = tasks.map(_.copy(removed   = true))

  private def getActor = system.actorOf(Props(new TodoHandlerActor(userId)))

  "AddTodoTask" should {
    "add and persist" in {
      val actor = getActor

      tasks.foreach { task =>
        actor ! AddTodoTask(task)
        expectMsg(task)
      }
      actor ! Shutdown

      val recover  = getActor
      val testTask = tasks.head

      recover ! GetTodoTaskById(testTask.taskId)
      expectMsg(Some(testTask))
    }
  }

  "GetTodoTaskById" should {
    "return Some(task) when matched by taskId and otherwise return None" in {
      val actor = getActor

      allTasksAreActive.foreach(task => actor ! AddTodoTask(task))
      receiveN(4)

      val testTask        = allTasksAreActive(3)
      val nonExistentTask = sample[UUIDString].value

      actor ! GetTodoTaskById(testTask.taskId)
      expectMsg(Some(testTask))

      actor ! GetTodoTaskById(nonExistentTask)
      expectMsg(None)
    }
  }

  "GetAllTodoTasks" should {
    "return all (non-removed) tasks and sort the result by taskId" in {
      val actor = getActor
      allTasksAreActive.foreach(task => actor ! AddTodoTask(task))
      receiveN(4)

      actor ! GetAllTodoTasks
      expectMsg(allTasksAreActive.sortBy(_.priority))
    }

    "not return any removed tasks" in {
      val actor    = getActor
      val randomId = sample[UUIDString].value
      val testTask = allTasksAreActive.head.copy(taskId = randomId)

      actor ! AddTodoTask(testTask)
      receiveN(1)

      allTasksAreRemoved.foreach(task => actor ! AddTodoTask(task))
      receiveN(4)

      actor ! GetAllTodoTasks
      expectMsg(List(testTask))
    }
  }

  "GetTodoTasksByTitle" should {
    "return non-removed tasks whose title matches the query string and sort te result by taskId" in {
      val taskFound  = allTasksAreActive.head.copy(title = "found")
      val taskBound  = allTasksAreActive(1).copy(title   = "bound")
      val taskSounds = allTasksAreActive(2).copy(title   = "sounds")
      val taskFund   = allTasksAreActive(3).copy(title   = "fund")

      val actor = getActor

      List(taskFound, taskBound, taskSounds, taskFund).foreach(task => actor ! AddTodoTask(task))
      receiveN(4)

      actor ! GetTodoTasksByTitle("bound")
      expectMsg(List(taskBound))

      actor ! GetTodoTasksByTitle("oun")
      expectMsg(List(taskFound, taskBound, taskSounds).sortBy(_.title))

      actor ! GetTodoTasksByTitle("und")
      expectMsg(List(taskFound, taskBound, taskSounds, taskFund).sortBy(_.title))

      actor ! GetTodoTasksByTitle("unds")
      expectMsg(List(taskSounds))

      val nonExistentTask = sample[UUIDString].value
      actor ! GetTodoTasksByTitle(nonExistentTask)
      expectMsg(List())
    }

    "not return any removed tasks" in {
      val actor    = getActor
      val randomId = sample[UUIDString].value
      val testTask = allTasksAreActive.head.copy(taskId = randomId, title = "same title all tasks")

      actor ! AddTodoTask(testTask)
      receiveN(1)

      val similarlyNamedRemovedTasks = allTasksAreRemoved.map(_.copy(title = testTask.title))
      similarlyNamedRemovedTasks.foreach(task => actor ! AddTodoTask(task))
      receiveN(4)

      actor ! GetTodoTasksByTitle(testTask.title)
      expectMsg(List(testTask))
    }
  }

  "UpdatePriority" should {
    "update priority for the given task and then return the number of affected rows (1)" in {
      val actor = getActor

      allTasksAreActive.foreach(task => actor ! AddTodoTask(task))
      receiveN(4)

      val newPriority = sample[PositiveInteger].value
      val testTask    = allTasksAreActive(2).copy(priority = newPriority)
      val otherTask   = allTasksAreActive.head

      actor ! UpdatePriority(testTask.taskId, newPriority)
      expectMsg(1)

      actor ! GetTodoTaskById(testTask.taskId)
      expectMsg(Some(testTask))

      actor ! GetTodoTaskById(otherTask.taskId)
      expectMsg(Some(otherTask))
    }

    "return 0 (affected rows when not matching by taskId" in {
      val actor = getActor

      tasks.foreach(task => actor ! AddTodoTask(task))
      receiveN(4)

      val nonExistentTask = sample[UUIDString].value
      val newPriority     = sample[PositiveInteger].value

      actor ! UpdatePriority(nonExistentTask, newPriority)
      expectMsg(0)
    }
  }

  "UpdateCompleted" should {
    "update completion status for the given task and return the number of affected rows (1)" in {
      val actor = getActor

      allTasksAreActive.foreach(task => actor ! AddTodoTask(task))
      receiveN(4)

      val testTask  = allTasksAreActive(3).copy(completed = true)
      val otherTask = allTasksAreActive(1)

      actor ! UpdateCompleted(testTask.taskId)
      expectMsg(1)

      actor ! GetTodoTaskById(testTask.taskId)
      expectMsg(Some(testTask))

      actor ! GetTodoTaskById(otherTask.taskId)
      expectMsg(Some(otherTask))
    }

    "return 0 (affected rows when not matching by taskId" in {
      val actor = getActor

      tasks.foreach(task => actor ! AddTodoTask(task))
      receiveN(4)

      val nonExistentTask = sample[UUIDString].value

      actor ! UpdateCompleted(nonExistentTask)
      expectMsg(0)
    }
  }

  "RemoveTask" should {
    "set given task to removed status amd return the number of affected rows (1)" in {
      val actor = getActor

      allTasksAreActive.foreach(task => actor ! AddTodoTask(task))
      receiveN(4)

      val testTask  = allTasksAreActive(3).copy(removed = true)
      val otherTask = allTasksAreActive(1)

      actor ! RemoveTask(testTask.taskId)
      expectMsg(1)

      actor ! GetTodoTaskById(testTask.taskId)
      expectMsg(Some(testTask))

      actor ! GetTodoTaskById(otherTask.taskId)
      expectMsg(Some(otherTask))
    }

    "return 0 (affected rows when not matching by taskId" in {
      val actor = getActor

      tasks.foreach(task => actor ! AddTodoTask(task))
      receiveN(4)

      val nonExistentTask = sample[UUIDString].value

      actor ! RemoveTask(nonExistentTask)
      expectMsg(0)
    }
  }
}
