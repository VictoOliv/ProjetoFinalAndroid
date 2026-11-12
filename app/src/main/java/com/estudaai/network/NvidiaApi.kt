package com.estudaai.network

import com.estudaai.data.ChatRequest
import com.estudaai.data.ChatResponse
import retrofit2.http.Body
import retrofit2.http.POST

// Endpoint de chat da NVIDIA (formato compatível com OpenAI).
interface NvidiaApi {
    @POST("chat/completions")
    suspend fun conversar(@Body pedido: ChatRequest): ChatResponse
}
