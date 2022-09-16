package me.anzop.todo.utils

import me.anzop.todo.http.dto.TodoTaskDto
import me.anzop.todo.models.TodoTask
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}

object ArbitraryTestData {

  case class PositiveInteger(value: Int) extends AnyVal

  case class UUIDString(value: String) extends AnyVal

  case class Title(value: String) extends AnyVal

  case class OneTodoTaskInput(task: TodoTaskDto) extends AnyVal

  case class OneTodoTask(task: TodoTask) extends AnyVal

  case class SeveralTodoTasks(tasks: List[TodoTask]) extends AnyVal

  implicit val arbitraryPositiveInteger: Arbitrary[PositiveInteger] = Arbitrary {
    Gen.posNum[Int].map(PositiveInteger)
  }

  implicit val arbitraryUUID: Arbitrary[UUIDString] = Arbitrary {
    Gen.uuid.map(id => UUIDString(id.toString)).retryUntil(_.value.nonEmpty)
  }

  implicit val arbitraryTitle: Arbitrary[Title] = Arbitrary {
    Gen.identifier.map(title => Title(title)).retryUntil(_.value.nonEmpty)
  }

  implicit val arbitraryTodoTaskInput: Arbitrary[OneTodoTaskInput] = Arbitrary {
    for {
      userId <- arbitrary[UUIDString]
      task   <- generateTodoTaskInput(userId.value)
    } yield {
      OneTodoTaskInput(task)
    }
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

  def generateTodoTaskInput(userId: String): Gen[TodoTaskDto] =
    for {
      task <- generateTodoTask(userId)
    } yield {
      TodoTaskDto(
        userId    = userId,
        taskId    = None,
        title     = task.title,
        priority  = Some(task.priority),
        completed = Some(task.completed)
      )
    }

  def generateTodoTask(userId: String): Gen[TodoTask] =
    for {
      taskId    <- arbitrary[UUIDString]
      title     <- arbitrary[Title]
      priority  <- arbitrary[PositiveInteger]
      completed <- arbitrary[Boolean]
    } yield {
      TodoTask(
        userId    = userId,
        taskId    = taskId.value,
        title     = title.value,
        priority  = priority.value,
        completed = completed
      )
    }

  def sample[A : Arbitrary]: A = arbitrary[A].sample.get
}
