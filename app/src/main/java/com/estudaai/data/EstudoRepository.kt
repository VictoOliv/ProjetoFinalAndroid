package com.estudaai.data

import android.util.Log
import com.estudaai.BuildConfig
import com.estudaai.network.NvidiaApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

// Fala com a API da NVIDIA (Retrofit) e com as anotações salvas (Room).
class EstudoRepository(
    private val api: NvidiaApi,
    private val anotacaoDao: AnotacaoDao,
    private val sessao: SessaoManager
) {

    suspend fun perguntar(
        tema: String,
        historico: List<ChatMessage>,
        pergunta: String
    ): Result<String> = withContext(Dispatchers.IO) {
        if (BuildConfig.NVIDIA_API_KEY.isBlank()) {
            return@withContext Result.failure(
                Exception("Chave da API NVIDIA não configurada. Veja o README do projeto.")
            )
        }
        try {
            val mensagens = buildList {
                add(ChatMessage("system", instrucoesDoTutor(tema)))
                addAll(historico.takeLast(MAX_HISTORICO))
                add(ChatMessage("user", pergunta))
            }
            val resposta = try {
                api.conversar(ChatRequest(model = BuildConfig.NVIDIA_MODEL, messages = mensagens))
            } catch (e: HttpException) {
                // Modelo principal sobrecarregado ou indisponível: tenta o reserva.
                if (e.code() !in CODIGOS_PARA_RESERVA || BuildConfig.NVIDIA_MODEL_RESERVA.isBlank()) throw e
                Log.w(TAG, "Modelo principal falhou (${e.code()}), usando o reserva")
                api.conversar(ChatRequest(model = BuildConfig.NVIDIA_MODEL_RESERVA, messages = mensagens))
            }
            val texto = resposta.choices.firstOrNull()?.message?.content?.trim()
            Log.d(TAG, "Resposta recebida (${texto?.length ?: 0} caracteres)")
            if (texto.isNullOrBlank()) {
                Result.failure(Exception("A IA não retornou uma resposta. Tente novamente."))
            } else {
                Result.success(texto)
            }
        } catch (e: HttpException) {
            Log.e(TAG, "Erro HTTP ${e.code()}", e)
            Result.failure(Exception(mensagemHttp(e.code())))
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Tempo esgotado", e)
            Result.failure(Exception("A IA demorou demais para responder. Tente novamente."))
        } catch (e: IOException) {
            Log.e(TAG, "Falha de rede", e)
            Result.failure(Exception("Sem conexão com a internet. Verifique sua rede e tente novamente."))
        } catch (e: SerializationException) {
            Log.e(TAG, "Resposta em formato inesperado", e)
            Result.failure(Exception("A resposta da IA veio em um formato inesperado."))
        }
    }

    // As anotações sempre seguem o usuário logado: trocou de conta, trocou a lista.
    @OptIn(ExperimentalCoroutinesApi::class)
    val anotacoes: Flow<List<Anotacao>> = sessao.usuarioId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else anotacaoDao.listarDoUsuario(id)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun buscarAnotacao(id: Long): Flow<Anotacao?> = sessao.usuarioId.flatMapLatest { usuarioId ->
        if (usuarioId == null) flowOf(null) else anotacaoDao.buscar(id, usuarioId)
    }

    suspend fun salvarAnotacao(tema: String, pergunta: String, resposta: String): Boolean =
        withContext(Dispatchers.IO) {
            val usuarioId = sessao.usuarioId.value ?: return@withContext false
            anotacaoDao.inserir(
                Anotacao(usuarioId = usuarioId, tema = tema, pergunta = pergunta, resposta = resposta)
            )
            true
        }

    suspend fun criarAnotacao(tema: String, titulo: String, texto: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            val usuarioId = sessao.usuarioId.value
            when {
                usuarioId == null -> Result.failure(Exception("Sessão encerrada. Entre novamente."))
                titulo.isBlank() -> Result.failure(Exception("Dê um título para a anotação."))
                texto.isBlank() -> Result.failure(Exception("Escreva o conteúdo da anotação."))
                else -> {
                    anotacaoDao.inserir(
                        Anotacao(
                            usuarioId = usuarioId,
                            tema = tema.trim().ifBlank { "Geral" },
                            pergunta = titulo.trim(),
                            resposta = texto.trim(),
                            geradaPorIa = false
                        )
                    )
                    Result.success(Unit)
                }
            }
        }

    suspend fun excluirAnotacao(id: Long) = withContext(Dispatchers.IO) {
        val usuarioId = sessao.usuarioId.value ?: return@withContext
        anotacaoDao.excluir(id, usuarioId)
    }

    private fun instrucoesDoTutor(tema: String): String {
        val contexto = tema.ifBlank { "estudos gerais" }
        return """
            Você é o EstudaAI, um tutor de estudos paciente e didático.
            Responda sempre em português do Brasil, de forma simples, clara e objetiva,
            em no máximo 3 parágrafos curtos, usando exemplos quando ajudar.
            Não use formatação Markdown (sem asteriscos, cerquilhas ou tabelas).
            O contexto do estudo do aluno é: $contexto.
            Use esse contexto e a conversa anterior para entender a dúvida.
        """.trimIndent()
    }

    private fun mensagemHttp(codigo: Int): String = when (codigo) {
        401, 403 -> "Chave da API NVIDIA inválida ou sem permissão."
        404 -> "Modelo de IA não encontrado. Confira o NVIDIA_MODEL."
        429 -> "Muitas perguntas seguidas. Aguarde alguns segundos e tente de novo."
        in 500..599 -> "O servidor da IA está indisponível no momento. Tente novamente."
        else -> "Erro ao falar com a IA (código $codigo)."
    }

    private companion object {
        const val TAG = "EstudoRepository"
        const val MAX_HISTORICO = 6
        val CODIGOS_PARA_RESERVA = setOf(404, 410, 429, 500, 502, 503, 504)
    }
}
