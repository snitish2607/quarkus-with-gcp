package org.nirp.service

import com.mongodb.client.model.Filters
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import com.mongodb.client.model.Updates
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.bson.types.ObjectId
import org.nirp.model.Todo
import org.nirp.repository.TodoRepository

@ApplicationScoped
class TodoService {

    @Inject
    lateinit var todoRepository: TodoRepository

    fun getTodos() = todoRepository.listAll()

    fun getTodosByUsername(username: String) = todoRepository.list("username", username)

    fun getTodoById(id: String) = todoRepository.findById(ObjectId(id))

    fun addTodo(todo: Todo) = todoRepository.persist(todo)

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