package com.estudaai.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.estudaai.EstudaAiApplication
import com.estudaai.data.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Autor { ALUNO, IA }

data class Mensagem(
    val id: Long,
    val autor: Autor,
    val texto: String,
    /** Para respostas da IA: a pergunta que originou a resposta. */
    val pergunta: String = "",
    val salva: Boolean = false
)

/** Estado da chamada à API mostrado na tela. */
sealed interface StatusIA {
    data object Ocioso : StatusIA
    data object Carregando : StatusIA
    data object Sucesso : StatusIA
    data class Erro(val mensagem: String) : StatusIA
}

data class EstudoUiState(
    val tema: String = "",
    val mensagens: List<Mensagem> = emptyList(),
    val status: StatusIA = StatusIA.Ocioso
)

class EstudoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as EstudaAiApplication).container.estudoRepository

    private val _uiState = MutableStateFlow(EstudoUiState())
    val uiState: StateFlow<EstudoUiState> = _uiState.asStateFlow()

    private var proximoId = 0L
    private var perguntaPendente: String? = null

    fun atualizarTema(tema: String) {
        _uiState.update { it.copy(tema = tema) }
    }

    fun enviar(pergunta: String) {
        val texto = pergunta.trim()
        if (texto.isEmpty() || _uiState.value.status == StatusIA.Carregando) return
        val historico = historicoParaContexto()
        _uiState.update {
            it.copy(mensagens = it.mensagens + Mensagem(proximoId++, Autor.ALUNO, texto))
        }
        pedirResposta(texto, historico)
    }

    fun tentarNovamente() {
        val pergunta = perguntaPendente ?: return
        if (_uiState.value.status == StatusIA.Carregando) return
        // A pergunta que falhou ficou sem resposta, então já fica fora do histórico.
        pedirResposta(pergunta, historicoParaContexto())
    }

    fun salvar(mensagem: Mensagem) {
        if (mensagem.autor != Autor.IA || mensagem.salva) return
        viewModelScope.launch {
            val tema = _uiState.value.tema.trim().ifBlank { "Geral" }
            val salvou = repository.salvarAnotacao(tema, mensagem.pergunta, mensagem.texto)
            if (salvou) {
                _uiState.update { estado ->
                    estado.copy(mensagens = estado.mensagens.map {
                        if (it.id == mensagem.id) it.copy(salva = true) else it
                    })
                }
            }
        }
    }

    fun novaConversa() {
        perguntaPendente = null
        _uiState.update { EstudoUiState(tema = it.tema) }
    }

    private fun pedirResposta(pergunta: String, historico: List<ChatMessage>) {
        perguntaPendente = pergunta
        _uiState.update { it.copy(status = StatusIA.Carregando) }
        viewModelScope.launch {
            repository.perguntar(_uiState.value.tema, historico, pergunta)
                .onSuccess { resposta ->
                    perguntaPendente = null
                    _uiState.update {
                        it.copy(
                            mensagens = it.mensagens +
                                Mensagem(proximoId++, Autor.IA, resposta, pergunta = pergunta),
                            status = StatusIA.Sucesso
                        )
                    }
                }
                .onFailure { erro ->
                    _uiState.update {
                        it.copy(status = StatusIA.Erro(erro.message ?: "Erro inesperado."))
                    }
                }
        }
    }

    // Conversa anterior enviada como contexto. Perguntas que ficaram sem
    // resposta (por erro) são ignoradas para não confundir a IA.
    private fun historicoParaContexto(): List<ChatMessage> {
        val mensagens = _uiState.value.mensagens
        return mensagens
            .filterIndexed { i, m -> m.autor == Autor.IA || mensagens.getOrNull(i + 1)?.autor == Autor.IA }
            .map { ChatMessage(if (it.autor == Autor.IA) "assistant" else "user", it.texto) }
    }
}
