package org.nirp.model

import io.quarkus.mongodb.panache.common.MongoEntity
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId

@MongoEntity(collection = "todo")
data class Todo (
    @BsonId var id: ObjectId = ObjectId.get(),
    var username: String,
    var title: String,
    var description: String,
    var imageUrl: String? = null
) {
    constructor() : this(ObjectId.get(), "", "", "", null)
}