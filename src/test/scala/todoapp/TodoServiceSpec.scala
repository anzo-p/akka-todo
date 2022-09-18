package todoapp

import akka.actor.{ActorRef, Props}
import todoapp.actor.TodoHandlerActor
import todoapp.actor.TodoHandlerActor.GetTodoTaskById
import todoapp.models.{TodoTask, TodoTaskParams}
import todoapp.utils.ArbitraryTestData.{sample, PositiveInteger, SeveralTodoTasks}
import todoapp.utils.BasePersistentActorSpec

import java.util.UUID

class TodoServiceSpec extends BasePersistentActorSpec {

  private val service = new TodoService {
    override def todoHandler(userId: UUID): ActorRef =
      system.actorOf(Props(new TodoHandlerActor(userId)))
  }

  private val setup  = sample[SeveralTodoTasks]
  private val userId = setup.tasks.head.userId
  private val tasks  = setup.tasks

  private val allTasksAreActive = tasks.map(_.copy(completed = false, removed = false))

  private def addSampleTasks(sample: List[TodoTask] = allTasksAreActive): Unit =
    sample.foreach(task => {
      service
        .addTodo(
          userId,
          TodoTaskParams(
            title     = task.title,
            priority  = Some(task.priority),
            completed = Some(task.completed)
          ))
        .futureValue
    })

  "addTodo" should {
    "create and add a tasks out of given parameters and response that task" in {
      service.getAllTodos(userId).futureValue mustBe Iterable()

      allTasksAreActive.foreach(task => {
        val response = service
          .addTodo(
            userId,
            TodoTaskParams(
              title     = task.title,
              priority  = Some(task.priority),
              completed = Some(task.completed)
            ))
          .futureValue

        response.userId mustBe task.userId
        response.title mustBe task.title
        response.priority mustBe task.priority
        response.completed mustBe task.completed
        response.removed mustBe task.removed
      })
    }
  }

  "getAllTodos" should {
    "respond an iterable of found todoTasks, sorted by priority" in {
      service.getAllTodos(userId).futureValue mustBe Iterable()

      addSampleTasks()
      val response = service.getAllTodos(userId).futureValue.toList

      response.size mustBe allTasksAreActive.length

      allTasksAreActive.sortBy(_.priority).zipWithIndex.foreach {
        case (task, ix) =>
          task.userId mustBe response(ix).userId
          task.title mustBe response(ix).title
          task.priority mustBe response(ix).priority
          task.completed mustBe response(ix).completed
          task.removed mustBe response(ix).removed
      }
    }

    "respond an empty iterable when no tasks are found" in {
      service.getAllTodos(userId).futureValue mustBe Iterable()
    }
  }

  "getAllTodosByTitle" should {
    "respond an iterable of found tasks, filtered and sorted by title" in {
      service.getAllTodos(userId).futureValue mustBe Iterable()

      val titleSearchKey = "title"
      allTasksAreActive.reverse.zipWithIndex.foreach {
        case (task, ix) =>
          service
            .addTodo(
              userId,
              TodoTaskParams(
                title     = s"$titleSearchKey $ix",
                priority  = Some(task.priority),
                completed = Some(task.completed)
              ))
            .futureValue
      }
      val response = service.getAllTodosByTitle(userId, titleSearchKey).futureValue

      response.size mustBe allTasksAreActive.length

      response.zipWithIndex.foreach {
        case (task, ix) =>
          task.title mustBe s"$titleSearchKey $ix"
      }
    }

    "should respond an empty iterable when no tasks are found" in {
      val nonExistentTitle = sample[String]

      service.getAllTodosByTitle(userId, nonExistentTitle).futureValue mustBe Iterable()
    }
  }

  "updatePriority" should {
    val testPriority = sample[PositiveInteger].value

    "update the priority of a task when a matching task found, then return 1 (row affected)" in {
      addSampleTasks()
      val testTask = service.getAllTodos(userId).futureValue.head

      testTask.priority must not be testPriority
      service.updatePriority(userId, testTask.taskId, testPriority).futureValue mustBe 1

      service.todoHandler(userId) ! GetTodoTaskById(testTask.taskId)

      expectMsg(Some(testTask.copy(priority = testPriority)))
    }

    "return 0 rows affected when no matching task found" in {
      addSampleTasks()
      val nonExistentTask = sample[UUID]

      service.updatePriority(userId, nonExistentTask, testPriority).futureValue mustBe 0
    }
  }

  "updateCompleted" should {
    "update the completed status of a task when a matching task found, then return 1 (row affected)" in {
      addSampleTasks()
      val testTask = service.getAllTodos(userId).futureValue.head

      testTask.completed mustBe false
      service.updateCompleted(userId, testTask.taskId).futureValue mustBe 1

      service.todoHandler(userId) ! GetTodoTaskById(testTask.taskId)

      expectMsg(Some(testTask.copy(completed = true)))
    }

    "return 0 rows affected when no matching task found" in {
      addSampleTasks()
      val nonExistentTask = sample[UUID]

      service.updateCompleted(userId, nonExistentTask).futureValue mustBe 0
    }
  }

  "removeTask" should {
    "set the removed status to true of a task when a matching task found, then return 1 (row affected)" in {
      addSampleTasks()
      val testTask = service.getAllTodos(userId).futureValue.head

      testTask.removed mustBe false
      service.removeTask(userId, testTask.taskId).futureValue mustBe 1

      service.todoHandler(userId) ! GetTodoTaskById(testTask.taskId)

      expectMsg(Some(testTask.copy(removed = true)))
    }

    "return 0 rows affected when no matching task found" in {
      addSampleTasks()
      val nonExistentTask = sample[UUID]

      service.removeTask(userId, nonExistentTask).futureValue mustBe 0
    }
  }
}
