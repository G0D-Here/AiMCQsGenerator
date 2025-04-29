package com.example.aivideonote.domain

import com.example.aivideonote.data.GeminiContent
import com.example.aivideonote.data.GeminiPart
import com.example.aivideonote.data.GeminiRequest
import com.example.aivideonote.screen.auth.apikeyToUse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class RepositoryApi @Inject constructor(
    private val api: GeminiApi,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    suspend fun getResponse(prompt: String): Result<String> = withContext(dispatcher) {
        try {
            require(prompt.isNotBlank()) { "Prompt must not be empty or blank." }

            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = prompt))
                    )
                )
            )
            val response = api.generateContent(apikeyToUse, request)
            val text = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "No Response from server"
            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

