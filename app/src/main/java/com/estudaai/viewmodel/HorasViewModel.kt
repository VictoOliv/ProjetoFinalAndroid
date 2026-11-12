package com.estudaai.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.estudaai.EstudaAiApplication
import com.estudaai.data.RegistroEstudo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class ResumoHoras(
    val totalMinutos: Int = 0,
    val ultimos7DiasMinutos: Int = 0,
    val sessoes: Int = 0
)

data class HorasUiState(
    val salvando: Boolean = false,
    val erro: String? = null
)

class HorasViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as EstudaAiApplication).container.horasEstudoRepository

    /** null enquanto o Room ainda não respondeu. */
    val registros: StateFlow<List<RegistroEstudo>?> = repository.registros
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val resumo: StateFlow<ResumoHoras> = repository.registros
        .map(::calcularResumo)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ResumoHoras())

    private val _uiState = MutableStateFlow(HorasUiState())
    val uiState: StateFlow<HorasUiState> = _uiState.asStateFlow()

    /** [aoConcluir] só roda quando o registro foi salvo (para limpar o formulário). */
    fun registrar(materia: String, horas: String, minutos: String, aoConcluir: () -> Unit) {
        if (_uiState.value.salvando) return
        val totalMinutos = (horas.toIntOrNull() ?: 0) * 60 + (minutos.toIntOrNull() ?: 0)
        viewModelScope.launch {
            _uiState.value = HorasUiState(salvando = true)
            val resultado = repository.registrar(materia, totalMinutos)
            _uiState.value = HorasUiState(erro = resultado.exceptionOrNull()?.message)
            if (resultado.isSuccess) aoConcluir()
        }
    }

    fun excluir(id: Long) {
        viewModelScope.launch { repository.excluir(id) }
    }

    private fun calcularResumo(registros: List<RegistroEstudo>): ResumoHoras {
        val inicioSemana = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
        return ResumoHoras(
            totalMinutos = registros.sumOf { it.minutos },
            ultimos7DiasMinutos = registros.filter { it.data >= inicioSemana }.sumOf { it.minutos },
            sessoes = registros.size
        )
    }
}
