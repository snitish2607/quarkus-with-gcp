package org.nirp.repository

import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import org.bson.types.ObjectId
import org.nirp.model.Todo

@ApplicationScoped
class TodoRepository : PanacheMongoRepositoryBase<Todo, ObjectId> {
}