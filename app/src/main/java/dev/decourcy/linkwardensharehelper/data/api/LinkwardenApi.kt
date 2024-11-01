package dev.decourcy.linkwardensharehelper.data.api

import dev.decourcy.linkwardensharehelper.data.model.LinkwardenRequest
import dev.decourcy.linkwardensharehelper.data.model.LinkwardenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface LinkwardenApi {
    @POST("/api/v1/links")
    suspend fun saveLink(
        @Header("Authorization") auth: String,
        @Body link: LinkwardenRequest
    ): Response<LinkwardenResponse>
}
