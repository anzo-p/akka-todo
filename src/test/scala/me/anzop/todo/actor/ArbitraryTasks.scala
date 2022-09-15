package me.anzop.todo.actor

import me.anzop.todo.models.TodoTask
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}

object ArbitraryTasks {

  case class PositiveInteger(value: Int) extends AnyVal

  case class UUIDString(value: String) extends AnyVal

  case class OneTodoTask(task: TodoTask)

  case class SeveralTodoTasks(tasks: List[TodoTask])

  implicit val arbitraryPositiveInteger: Arbitrary[PositiveInteger] = Arbitrary {
    Gen.posNum[Int].map(PositiveInteger)
  }

  implicit val arbitraryUUID: Arbitrary[UUIDString] = Arbitrary {
    Gen.uuid.map(id => UUIDString(id.toString)).retryUntil(_.value.nonEmpty)
  }

  implicit val arbitraryTodoTask: Arbitrary[OneTodoTask] = Arbitrary {
    for {
      userId <- arbitrary[UUIDString]
      task   <- generateTodoTask(userId.value)
    } yield {
      OneTodoTask(task)
    }
  }

  implicit val arbitraryListOfTodoTasks: Arbitrary[SeveralTodoTasks] = Arbitrary {
    for {
      userId <- arbitrary[UUIDString]
      tasks  <- Gen.listOfN(4, generateTodoTask(userId.value))
    } yield {
      SeveralTodoTasks(tasks)
    }
  }

  def generateTodoTask(userId: String): Gen[TodoTask] =
    for {
      taskId    <- arbitrary[UUIDString]
      title     <- arbitrary[String]
      priority  <- arbitrary[PositiveInteger]
      completed <- arbitrary[Boolean]
    } yield {
      TodoTask(
        userId    = userId,
        taskId    = taskId.value,
        title     = title,
        priority  = priority.value,
        completed = completed
      )
    }

  def sample[A : Arbitrary]: A = arbitrary[A].sample.get
}
