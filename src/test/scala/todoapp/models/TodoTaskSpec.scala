package todoapp.models

import todoapp.utils.ArbitraryTestData.{sample, OneTodoTask, PositiveInteger}
import todoapp.utils.BaseSpec

import java.util.UUID

class TodoTaskSpec extends BaseSpec {

  "TodoTask.apply" should {
    "apply create itself out of TodoTaskParams" when {
      val userId = sample[UUID]

      "fully populated" in {
        val params = TodoTaskParams(
          title     = sample[String],
          priority  = Some(sample[PositiveInteger].value),
          completed = Some(sample[Boolean])
        )
        val todo = TodoTask(userId, params)

        todo.title mustBe params.title
        todo.priority mustBe params.priority.get
        todo.completed mustBe params.completed.get
      }
      "optional fields" in {
        val params = TodoTaskParams(
          title     = sample[String],
          priority  = None,
          completed = None
        )
        val todo = TodoTask(userId, params)

        todo.title mustBe params.title
        todo.priority mustBe 0
        todo.completed mustBe false
      }
    }

    "apply create itself out of another TodoTask and TodoTaskParams" when {
      val existingTodo = sample[OneTodoTask].task

      "fully populated" in {
        val params = TodoTaskParams(
          title     = sample[String],
          priority  = Some(sample[PositiveInteger].value),
          completed = Some(sample[Boolean])
        )
        val updatedTodo = TodoTask(existingTodo, params)

        updatedTodo.userId mustBe existingTodo.userId
        updatedTodo.taskId mustBe existingTodo.taskId
        updatedTodo.title mustBe params.title
        updatedTodo.priority mustBe params.priority.get
        updatedTodo.completed mustBe params.completed.get
        updatedTodo.removed mustBe existingTodo.removed
      }
      "optional fields" in {
        val params = TodoTaskParams(
          title     = sample[String],
          completed = None,
          priority  = None
        )
        val updatedTodo = TodoTask(existingTodo, params)

        updatedTodo.userId mustBe existingTodo.userId
        updatedTodo.taskId mustBe existingTodo.taskId
        updatedTodo.title mustBe params.title
        updatedTodo.priority mustBe existingTodo.priority
        updatedTodo.completed mustBe existingTodo.completed
        updatedTodo.removed mustBe existingTodo.removed
      }
    }
  }
}
