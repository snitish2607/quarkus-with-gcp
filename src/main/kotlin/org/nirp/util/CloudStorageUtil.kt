package org.nirp.util

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.concurrent.TimeUnit

@ApplicationScoped
class CloudStorageUtil {

    @Inject
    lateinit var storage: Storage

    fun upload(
        bucketName: String,
        filePath: String,
        fileName: String,
        content: ByteArray
    ) {
        val blobName = filePath + fileName

        val blobId = BlobId.of(bucketName, blobName)

        val contentType = when {
            fileName.endsWith(".jpeg") -> "image/jpeg"
            fileName.endsWith(".jpg") -> "image/jpeg"
            fileName.endsWith(".png") -> "image/png"
            else -> "application/octet-stream"
        }

        val blobInfo = BlobInfo.newBuilder(blobId)
            .setContentType(contentType)
            .build()

        storage.create(blobInfo,content)
    }

    fun download(
        bucketName : String,
        filePath: String, // Will include the filename and extension
    ) : ByteArray {
        val blob = storage.get(bucketName,filePath) ?: throw Exception("File not found")

        return blob.getContent()
    }

    fun generateSignedUrl(
        bucketName: String,
        filePath: String
    ) : String {

        val blobId = BlobId.of(bucketName,filePath)

        return storage.signUrl(
            BlobInfo.newBuilder(blobId).build(),
            15,
            TimeUnit.MINUTES,
            Storage.SignUrlOption.withV4Signature()
        ).toString()
    }
}