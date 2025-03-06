package org.nirp.client

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import org.nirp.dto.TodoResponse

@RegisterRestClient
@Path("/todos")
interface TodoClient {

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getTodoById(id: Int): TodoResponse
}