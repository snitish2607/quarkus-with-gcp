package org.nirp.client

import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import org.jboss.resteasy.reactive.RestForm
import org.nirp.dto.UploadResponse
import java.io.File

@RegisterRestClient
@Path("/upload")
interface UploadClient {

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    fun uploadImage(
        @RestForm("file") file: File,
        @RestForm("filename") filename: String
    ) : UploadResponse
}