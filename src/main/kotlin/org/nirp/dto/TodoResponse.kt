package org.nirp.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TodoResponse (
    @JsonProperty("userId") val userId: Int,
    @JsonProperty("id") val id: Int,
    @JsonProperty("title") val title: String,
    @JsonProperty("completed") val completed: Boolean
)
