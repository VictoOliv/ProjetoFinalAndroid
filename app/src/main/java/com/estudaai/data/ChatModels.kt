package com.estudaai.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Data classes do JSON trocado com a API da NVIDIA.

@Serializable
data class ChatMessage(
    val role: String,
    val content: String
)

@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Double = 0.4,
    // Folga para modelos que "pensam" antes de responder (reasoning) não cortarem a resposta.
    @SerialName("max_tokens") val maxTokens: Int = 2048,
    val stream: Boolean = false
)

@Serializable
data class ChatResponse(
    val choices: List<ChatChoice> = emptyList()
)

@Serializable
data class ChatChoice(
    val message: ChatResponseMessage? = null
)

@Serializable
data class ChatResponseMessage(
    val role: String? = null,
    val content: String? = null
)
