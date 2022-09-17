package me.anzop.todo.http

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import me.anzop.todo.http.dto.{TodoPriorityDto, TodoTaskDto}
import me.anzop.todo.http.system.{ErrorResponse, TodoMarshalling}
import me.anzop.todo.http.validation.{EmptyField, NegativeValue}
import me.anzop.todo.models.TodoTask
import me.anzop.todo.utils.ArbitraryTestData.{sample, OneTodoTask, OneTodoTaskInput, PositiveInteger, Title}
import me.anzop.todo.utils.BaseSpec
import me.anzop.todo.utils.mocks.TodoServiceMocks
import org.scalamock.scalatest.MockFactory

import java.util.UUID

class TodoRoutesSpec
    extends BaseSpec
    with MockFactory
    with TodoServiceMocks
    with ScalatestRouteTest
    with TodoMarshalling {

  private val userId = sample[UUID]

  private val todoRoutes = new TodoRoutes(mockTodoService).routes

  "GET all tasks" should {

    "200 - respond an empty list when no data" in {
      mockGetAllTodos(userId, List())

      Get(s"/api/v1/todos/$userId") ~> todoRoutes ~> check {
        status mustBe StatusCodes.OK
        entityAs[Iterable[TodoTaskDto]] mustBe List()
      }
    }

    "200 - respond a list with one item" in {
      val task1 = sample[OneTodoTask].task

      mockGetAllTodos(userId, List(task1))

      Get(s"/api/v1/todos/$userId") ~> todoRoutes ~> check {
        status mustBe StatusCodes.OK
        entityAs[Iterable[TodoTaskDto]] mustBe List(TodoTaskDto.fromModel(task1))
      }
    }

    "200 - respond a list with multiple items" in {
      val task1 = sample[OneTodoTask].task
      val task2 = sample[OneTodoTask].task

      mockGetAllTodos(userId, List(task1, task2))

      Get(s"/api/v1/todos/$userId") ~> todoRoutes ~> check {
        status mustBe StatusCodes.OK
        entityAs[Iterable[TodoTaskDto]] mustBe List(TodoTaskDto.fromModel(task1), TodoTaskDto.fromModel(task2))
      }
    }
  }

  "GET all tasks filtered by title" should {
    val testTitle = sample[Title].value

    "200 - respond an empty list when no data" in {
      mockGetAllTodosByTitle(userId, testTitle, List())

      Get(s"/api/v1/todos/$userId?title=$testTitle") ~> todoRoutes ~> check {
        status mustBe StatusCodes.OK
        entityAs[Iterable[TodoTaskDto]] mustBe List()
      }
    }

    "200 - respond a list with one item" in {
      val task1 = sample[OneTodoTask].task

      mockGetAllTodosByTitle(userId, testTitle, List(task1))

      Get(s"/api/v1/todos/$userId?title=$testTitle") ~> todoRoutes ~> check {
        status mustBe StatusCodes.OK
        entityAs[Iterable[TodoTaskDto]] mustBe List(TodoTaskDto.fromModel(task1))
      }
    }

    "200 - respond a list with multiple items" in {
      val task1 = sample[OneTodoTask].task
      val task2 = sample[OneTodoTask].task

      mockGetAllTodosByTitle(userId, testTitle, List(task1, task2))

      Get(s"/api/v1/todos/$userId?title=$testTitle") ~> todoRoutes ~> check {
        status mustBe StatusCodes.OK
        entityAs[Iterable[TodoTaskDto]] mustBe List(TodoTaskDto.fromModel(task1), TodoTaskDto.fromModel(task2))
      }
    }
  }

  "POST a todo task" should {

    "201 - store validating input and respond as TodoTask" in {
      val payload = sample[OneTodoTaskInput].task
      val params  = payload.toParams

      mockAddTodo(userId, params, TodoTask(userId, params))

      Post(s"/api/v1/todos/$userId", payload) ~> todoRoutes ~> check {
        status mustBe StatusCodes.Created
        val body = entityAs[Iterable[TodoTaskDto]].head
        body.title mustBe payload.title
        body.priority mustBe payload.priority
        body.completed mustBe payload.completed
      }
    }

    "400 - respond validation failures without processing any further" in {
      val payload = TodoTaskDto(
        userId    = Some(sample[UUID].toString),
        taskId    = Some(sample[UUID].toString),
        title     = "",
        priority  = Some(sample[PositiveInteger].value * -1),
        completed = Some(sample[Boolean])
      )

      Post(s"/api/v1/todos/$userId", payload) ~> todoRoutes ~> check {
        status mustBe StatusCodes.BadRequest

        val body = entityAs[ErrorResponse]

        body.reason mustBe List(
          EmptyField("title").errorMessage,
          NegativeValue("priority").errorMessage
        )
      }
    }
  }

  "PATCH priority" should {
    "200 - store the priority change and respond success when matching task found" in {
      val taskId  = sample[UUID]
      val payload = TodoPriorityDto(sample[PositiveInteger].value)

      mockUpdatePriority(userId, taskId, payload.priority, 1)

      Patch(s"/api/v1/todos/$userId/task/$taskId/priority", payload) ~> todoRoutes ~> check {
        status mustBe StatusCodes.OK
      }
    }

    "400 - respond validation failure without processing any further" in {
      val taskId  = sample[UUID]
      val payload = TodoPriorityDto(sample[PositiveInteger].value * -1)

      Patch(s"/api/v1/todos/$userId/task/$taskId/priority", payload) ~> todoRoutes ~> check {
        status mustBe StatusCodes.BadRequest

        val body = entityAs[ErrorResponse]

        body.reason mustBe List(
          NegativeValue("priority").errorMessage
        )
      }
    }

    "404 - respond not found when no matching task found" in {
      val taskId  = sample[UUID]
      val payload = TodoPriorityDto(sample[PositiveInteger].value)

      mockUpdatePriority(userId, taskId, payload.priority, 1)

      Patch(s"/api/v1/todos/$userId/task/$taskId/priority", payload) ~> todoRoutes ~> check {
        status mustBe StatusCodes.OK
      }
    }
  }

  "PATCH completion" should {
    "200 - store the completion change and respond success when matching task found" in {
      val taskId = sample[UUID]

      mockUpdateCompleted(userId, taskId, 1)

      Patch(s"/api/v1/todos/$userId/task/$taskId/completed") ~> todoRoutes ~> check {
        status mustBe StatusCodes.OK
      }
    }

    "404 - respond not found when no matching task found" in {
      val taskId = sample[UUID]

      mockUpdateCompleted(userId, taskId, 1)

      Patch(s"/api/v1/todos/$userId/task/$taskId/completed") ~> todoRoutes ~> check {
        status mustBe StatusCodes.OK
      }
    }
  }

  "PATCH remove task" should {
    "200 - store the removal and respond success when matching task found" in {
      val taskId = sample[UUID]

      mockRemoveTask(userId, taskId, 1)

      Delete(s"/api/v1/todos/$userId/task/$taskId") ~> todoRoutes ~> check {
        status mustBe StatusCodes.OK
      }
    }

    "404 - respond not found when no matching task found" in {
      val taskId = sample[UUID]

      mockRemoveTask(userId, taskId, 1)

      Delete(s"/api/v1/todos/$userId/task/$taskId") ~> todoRoutes ~> check {
        status mustBe StatusCodes.OK
      }
    }
  }
}
