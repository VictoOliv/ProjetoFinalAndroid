package com.estudaai.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.estudaai.EstudaAiApplication
import com.estudaai.data.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AuthUiState(
    val carregando: Boolean = false,
    val erro: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as EstudaAiApplication).container.usuarioRepository

    /** null = ninguém logado. A navegação observa este valor. */
    val usuarioId: StateFlow<Long?> = repository.usuarioId

    val usuario: StateFlow<Usuario?> = repository.usuarioLogado
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun entrar(email: String, senha: String) = executar {
        repository.entrar(email, senha)
    }

    fun cadastrar(nome: String, email: String, senha: String, confirmacao: String) = executar {
        repository.cadastrar(nome, email, senha, confirmacao)
    }

    fun sair() {
        repository.sair()
        _uiState.value = AuthUiState()
    }

    fun limparErro() {
        _uiState.value = _uiState.value.copy(erro = null)
    }

    private fun executar(acao: suspend () -> Result<Unit>) {
        if (_uiState.value.carregando) return
        viewModelScope.launch {
            _uiState.value = AuthUiState(carregando = true)
            val resultado = acao()
            _uiState.value = AuthUiState(erro = resultado.exceptionOrNull()?.message)
        }
    }
}
