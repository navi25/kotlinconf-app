package org.jetbrains.kotlinconf.api

import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.*
import io.ktor.client.response.HttpResponse
import io.ktor.http.*
import kotlinx.io.core.use
import org.jetbrains.kotlinconf.data.AllData
import org.jetbrains.kotlinconf.data.Favorite
import org.jetbrains.kotlinconf.data.Vote
import org.jetbrains.kotlinconf.data.VotingCode

internal expect val END_POINT: String

class KotlinConfApi {

    private val client = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer().apply {
                setMapper(AllData::class, AllData.serializer())
                setMapper(Favorite::class, Favorite.serializer())
                setMapper(Vote::class, Vote.serializer())
            }
        }
        install(ExpectSuccess)
    }

    suspend fun createUser(userId: String): Boolean = client.request<HttpResponse> {
        apiUrl("users", null)
        method = HttpMethod.Post
        body = userId
    }.use {
        it.status.isSuccess()
    }

    suspend fun getAll(userId: String?): AllData = client.get {
        apiUrl("all", userId)
    }

    suspend fun postFavorite(favorite: Favorite, userId: String): Unit = client.post {
        apiUrl("favorites", userId)
        json()
        body = favorite
    }

    suspend fun deleteFavorite(favorite: Favorite, userId: String): Unit = client.delete {
        apiUrl("favorites", userId)
        json()
        body = favorite
    }

    suspend fun postVote(vote: Vote, userId: String): Unit = client.post {
        apiUrl("votes", userId)
        json()
        body = vote
    }

    suspend fun deleteVote(vote: Vote, userId: String): Unit = client.delete {
        apiUrl("votes", userId)
        json()
        body = vote
    }

    suspend fun verifyCode(code: VotingCode): Unit = client.get {
        apiUrl("users/verify/$code", null)
    }

    private fun HttpRequestBuilder.json() {
        contentType(ContentType.Application.Json)
    }

    private fun HttpRequestBuilder.apiUrl(path: String, userId: String?) {
        if (userId != null) {
            header(HttpHeaders.Authorization, "Bearer $userId")
        }
        header(HttpHeaders.CacheControl, "no-cache")
        url {
            takeFrom(END_POINT)
            encodedPath = path
        }
    }
}