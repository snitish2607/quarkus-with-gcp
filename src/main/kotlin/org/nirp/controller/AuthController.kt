package org.nirp.controller

import io.smallrye.jwt.build.Jwt
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.nirp.dto.AuthRequest
import org.nirp.dto.AuthResponse
import org.nirp.dto.User
import java.time.Duration

@Path("/auth")
class AuthController {

    val users = mapOf(
        "nirp-admin" to User("nirp-admin", "password", listOf("admin")),
        "nirp-user" to User("nirp-user", "password", listOf("user")),
    )


    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    fun authenticate(
        authRequest: AuthRequest
    ) : Response {
        val user = users[authRequest.username]
        if(user != null && user.password == authRequest.password) {
            val token = Jwt.issuer("quarkus-app")
                .subject(authRequest.username)
                .claim("groups" , user.roles)
                .expiresIn(Duration.ofHours(2))
                .sign()
            return Response.ok(AuthResponse(token)).build()
        }
        return Response.status(Response.Status.UNAUTHORIZED).build()
    }

}