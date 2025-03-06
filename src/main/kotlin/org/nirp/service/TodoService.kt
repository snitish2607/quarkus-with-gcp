package org.nirp.service

import com.google.cloud.logging.LogEntry
import com.google.cloud.logging.Logging
import com.google.cloud.logging.Payload
import com.google.cloud.logging.Severity
import com.mongodb.client.model.Filters
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import com.mongodb.client.model.Updates
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.bson.types.ObjectId
import org.nirp.config.LoggingConfig
import org.nirp.model.Todo
import org.nirp.repository.TodoRepository
import java.time.Instant

@ApplicationScoped
class TodoService {

    @Inject
    lateinit var todoRepository: TodoRepository

    @Inject
    lateinit var log: Logging

    fun getTodos() = todoRepository.listAll()

    fun getTodosByUsername(username: String) = todoRepository.list("username", username)

    fun getTodoById(id: String) = todoRepository.findById(ObjectId(id))

    fun addTodo(todo: Todo) {
        todoRepository.persist(todo)
        log.write(listOf(LogEntry.newBuilder(Payload.StringPayload.of("Todo added"))
            .setSeverity(Severity.DEBUG)
            .setLogName(LoggingConfig.LOG_NAME)
            .setTimestamp(Instant.now())
            .build()));
    }

    fun deleteTodoById(id: String) = todoRepository.deleteById(ObjectId(id))

    fun updateTodo(updatedTodo: Todo): Todo {
        todoRepository.update(updatedTodo)
        return updatedTodo
    }

    fun updateTodoImageUrl(
        id: String,
        imageUrl: String
    ) : Todo? {
        val filter = Filters.eq("_id", ObjectId(id))
        val update = Updates.set(Todo::imageUrl.name,imageUrl)

        return todoRepository.mongoCollection().findOneAndUpdate(
            filter, update, FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        )
    }
}