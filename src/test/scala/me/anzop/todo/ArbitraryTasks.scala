package me.anzop.todo

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}

object ArbitraryTasks {

  case class PositiveInteger(value: Int) extends AnyVal

  case class UUIDString(value: String) extends AnyVal

  case class TodoTasks(tasks: List[TodoTask])

  implicit val arbitraryUUID: Arbitrary[UUIDString] = Arbitrary {
    Gen.uuid.map(id => UUIDString(id.toString))
  }

  implicit val arbitraryPositiveInteger: Arbitrary[PositiveInteger] = Arbitrary {
    Gen.posNum[Int].map(PositiveInteger)
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

  implicit val arbitraryListOfTodoTasks: Arbitrary[TodoTasks] = Arbitrary {
    for {
      userId <- arbitrary[UUIDString]
      tasks  <- Gen.listOfN(4, generateTodoTask(userId.value))
    } yield {
      TodoTasks(tasks)
    }
  }

  def sample[A : Arbitrary]: A = arbitrary[A].sample.get
}
