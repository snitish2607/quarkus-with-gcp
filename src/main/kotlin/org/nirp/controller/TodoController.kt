package org.nirp.controller

import io.quarkus.security.Authenticated
import jakarta.annotation.security.RolesAllowed
import jakarta.inject.Inject
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.SecurityContext
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.jboss.resteasy.reactive.RestForm
import org.nirp.dto.TodoRequest
import org.nirp.model.Todo
import org.nirp.service.TodoService
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.nirp.client.TodoClient
import org.nirp.util.CloudStorageUtil
import java.nio.file.StandardCopyOption
import kotlin.random.Random

@Path("/todos")
@Authenticated
class TodoController {

    @Inject
    lateinit var todoService: TodoService

    @Inject
    @RestClient
    lateinit var todoClient: TodoClient

    @Inject
    lateinit var cloudStorageUtil: CloudStorageUtil

    @ConfigProperty(name = "application.quarkus-with-gcp.google-cloud-storage.bucket-name")
    lateinit var bucketName: String

    @GET
    fun getTodos(@Context securityContext: SecurityContext) : Response {

        val username = securityContext.userPrincipal.name

        val todos = todoService.getTodosByUsername(username)
        return Response.ok(todos).build()
    }

    @GET
    @Path("/all")
    @RolesAllowed("admin")
    fun getAllTodos() : Response {
        val todos = todoService.getTodos()
        return Response.ok(todos).build()
    }

    @POST
    fun createTodo(
        todoRequest: TodoRequest
    ) : Response {
        todoService.addTodo(Todo(
            title = todoRequest.title,
            description = todoRequest.description,
            username = todoRequest.username
        ))
        return Response.ok().build()
    }

    @PUT
    @Path("/{id}")
    fun updateTodo(
        @PathParam("id") id: String,
        todoRequest : TodoRequest
    ) : Response {
        val existingTodo = todoService.getTodoById(id) ?: return Response.status(Response.Status.NOT_FOUND).build()

        existingTodo.title = todoRequest.title
        existingTodo.description = todoRequest.description

        todoService.updateTodo(existingTodo)

        return Response.ok(existingTodo).build()
    }

    @DELETE
    @Path("/{id}")
    fun deleteTodo(
        @PathParam("id") id: String
    ) : Response {
        todoService.deleteTodoById(id)
        return Response.ok().build()
    }

    @PUT
    @Path("{id}/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    fun uploadFile(
        @PathParam("id") id: String,
        @RestForm("file") file: FileUpload
    ) : Response {

        if(file.fileName().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).build()
        }

//        val uploadDir = Paths.get(System.getProperty("java.io.tmpdir"), "uploads")
//        Files.createDirectories(uploadDir)

        val fileName = UUID.randomUUID().toString() + ".jpg"
        //val filePath = uploadDir.resolve(fileName)

//        file.uploadedFile().toFile().inputStream().use {
//            Files.copy(it, filePath, StandardCopyOption.REPLACE_EXISTING)
//        }

        file.uploadedFile().toFile().inputStream().use {
            cloudStorageUtil.upload(
                bucketName,
                "uploads/",
                fileName,
                it.readBytes()
            )
        }

        val imageUrl = "uploads/$fileName"
        val updatedTodo = todoService.updateTodoImageUrl(id, imageUrl)

        return Response.ok(updatedTodo).build()

    }

    @GET
    @Path("{id}/download")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    fun downloadFile(
        @PathParam("id") id: String
    ) : Response {
        val todo = todoService.getTodoById(id) ?: return Response.status(Response.Status.NOT_FOUND).build()

        if(todo.imageUrl == null) {
            return Response.status(Response.Status.PRECONDITION_FAILED).build()
        }

        val fileBytes = cloudStorageUtil.download(
            bucketName,
            todo.imageUrl!!
        )

        return Response.ok(fileBytes)
            .type("image/jpeg")
            .header("Content-Disposition", "attachment; filename=${todo.imageUrl!!.split("/").last()}")
            .build()
    }

    @GET
    @Path("{id}/signed-url")
    fun getSignedUrl(
        @PathParam("id") id: String
    ) : Response {
        val todo = todoService.getTodoById(id) ?: return Response.status(Response.Status.NOT_FOUND).build()

        if(todo.imageUrl == null) {
            return Response.status(Response.Status.PRECONDITION_FAILED).build()
        }

        val signedUrl = cloudStorageUtil.generateSignedUrl(
            bucketName,
            todo.imageUrl!!
        )

        return Response.ok(signedUrl).build()
    }

    @GET
    @Path("/rest-client/{id}")
    fun getTodosFromRestClient(
        @PathParam("id") id: Int
    ) : Response {
        val todo = todoClient.getTodoById(id)

        println(todo.title)
        println(todo.id)

        return Response.ok(todo).build()
    }
}