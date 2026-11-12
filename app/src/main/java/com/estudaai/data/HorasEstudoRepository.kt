package com.estudaai.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext

// Registro das horas estudadas, sempre do usuário logado.
class HorasEstudoRepository(
    private val registroDao: RegistroEstudoDao,
    private val sessao: SessaoManager
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    val registros: Flow<List<RegistroEstudo>> = sessao.usuarioId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else registroDao.listarDoUsuario(id)
    }

    suspend fun registrar(materia: String, minutos: Int): Result<Unit> = withContext(Dispatchers.IO) {
        val usuarioId = sessao.usuarioId.value
        when {
            usuarioId == null -> falha("Sessão encerrada. Entre novamente.")
            materia.isBlank() -> falha("Informe a matéria estudada.")
            minutos <= 0 -> falha("Informe quanto tempo você estudou.")
            minutos > MAXIMO_MINUTOS -> falha("O máximo é 24 horas por registro.")
            else -> {
                registroDao.inserir(
                    RegistroEstudo(usuarioId = usuarioId, materia = materia.trim(), minutos = minutos)
                )
                Result.success(Unit)
            }
        }
    }

    suspend fun excluir(id: Long) = withContext(Dispatchers.IO) {
        val usuarioId = sessao.usuarioId.value ?: return@withContext
        registroDao.excluir(id, usuarioId)
    }

    private fun falha(mensagem: String): Result<Unit> = Result.failure(Exception(mensagem))

    private companion object {
        const val MAXIMO_MINUTOS = 24 * 60
    }
}
