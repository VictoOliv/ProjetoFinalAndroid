package com.estudaai.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.estudaai.EstudaAiApplication
import com.estudaai.data.Anotacao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AnotacoesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as EstudaAiApplication).container.estudoRepository

    /** null enquanto o Room ainda não respondeu. */
    val anotacoes: StateFlow<List<Anotacao>?> = repository.anotacoes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Erro de validação ao criar uma anotação própria. */
    private val _erroNovaAnotacao = MutableStateFlow<String?>(null)
    val erroNovaAnotacao: StateFlow<String?> = _erroNovaAnotacao.asStateFlow()

    fun buscar(id: Long): Flow<Anotacao?> = repository.buscarAnotacao(id)

    /** [aoConcluir] só roda quando a anotação foi salva (para fechar o formulário). */
    fun criar(tema: String, titulo: String, texto: String, aoConcluir: () -> Unit) {
        viewModelScope.launch {
            val resultado = repository.criarAnotacao(tema, titulo, texto)
            _erroNovaAnotacao.value = resultado.exceptionOrNull()?.message
            if (resultado.isSuccess) aoConcluir()
        }
    }

    fun limparErroNovaAnotacao() {
        _erroNovaAnotacao.value = null
    }

    /** [aoConcluir] só roda depois que o Room apagou a linha. */
    fun excluir(id: Long, aoConcluir: () -> Unit) {
        viewModelScope.launch {
            repository.excluirAnotacao(id)
            aoConcluir()
        }
    }
}
