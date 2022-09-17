package me.anzop.todo.utils

import me.anzop.todo.http.dto.TodoTaskDto
import me.anzop.todo.models.TodoTask
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}

import java.util.UUID

object ArbitraryTestData {

  case class PositiveInteger(value: Int)

  case class Title(value: String)

  case class OneTodoTaskInput(task: TodoTaskDto)

  case class OneTodoTask(task: TodoTask)

  case class SeveralTodoTasks(tasks: List[TodoTask])

  implicit val arbitraryPositiveInteger: Arbitrary[PositiveInteger] = Arbitrary {
    Gen.posNum[Int].map(PositiveInteger)
  }

  implicit val arbitraryTitle: Arbitrary[Title] = Arbitrary {
    Gen.identifier.map(title => Title(title)).retryUntil(_.value.nonEmpty)
  }

  implicit val arbitraryTodoTaskInput: Arbitrary[OneTodoTaskInput] = Arbitrary {
    for {
      userId <- arbitrary[UUID]
      task   <- generateTodoTaskInput(userId)
    } yield {
      OneTodoTaskInput(task)
    }
  }

  implicit val arbitraryTodoTask: Arbitrary[OneTodoTask] = Arbitrary {
    for {
      userId <- arbitrary[UUID]
      task   <- generateTodoTask(userId)
    } yield {
      OneTodoTask(task)
    }
  }

  implicit val arbitraryListOfTodoTasks: Arbitrary[SeveralTodoTasks] = Arbitrary {
    for {
      userId <- arbitrary[UUID]
      tasks  <- Gen.listOfN(4, generateTodoTask(userId))
    } yield {
      SeveralTodoTasks(tasks)
    }
  }

  def generateTodoTaskInput(userId: UUID): Gen[TodoTaskDto] =
    for {
      task <- generateTodoTask(userId)
    } yield {
      TodoTaskDto(
        userId    = Some(userId.toString),
        taskId    = None,
        title     = task.title,
        priority  = Some(task.priority),
        completed = Some(task.completed)
      )
    }

  def generateTodoTask(userId: UUID): Gen[TodoTask] =
    for {
      taskId    <- arbitrary[UUID]
      title     <- arbitrary[Title]
      priority  <- arbitrary[PositiveInteger]
      completed <- arbitrary[Boolean]
    } yield {
      TodoTask(
        userId    = userId,
        taskId    = taskId,
        title     = title.value,
        priority  = priority.value,
        completed = completed
      )
    }

  def sample[A : Arbitrary]: A = arbitrary[A].sample.get
}
