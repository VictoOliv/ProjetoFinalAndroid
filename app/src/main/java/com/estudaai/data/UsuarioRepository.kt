package com.estudaai.data

import android.database.sqlite.SQLiteConstraintException
import android.util.Patterns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext

// Toda a regra de cadastro, login e sessão fica aqui, nunca na tela.
class UsuarioRepository(
    private val usuarioDao: UsuarioDao,
    private val sessao: SessaoManager
) {
    val usuarioId: StateFlow<Long?> = sessao.usuarioId

    @OptIn(ExperimentalCoroutinesApi::class)
    val usuarioLogado: Flow<Usuario?> = sessao.usuarioId.flatMapLatest { id ->
        if (id == null) flowOf(null) else usuarioDao.observarPorId(id)
    }

    suspend fun cadastrar(
        nome: String,
        email: String,
        senha: String,
        confirmacao: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val emailNormalizado = email.trim().lowercase()
        when {
            nome.isBlank() -> falha("Informe seu nome.")
            !emailValido(emailNormalizado) -> falha("Informe um e-mail válido.")
            senha.length < TAMANHO_MINIMO_SENHA -> falha("A senha precisa ter 6 ou mais caracteres.")
            senha != confirmacao -> falha("As senhas não conferem.")
            usuarioDao.buscarPorEmail(emailNormalizado) != null -> falha("Este e-mail já está cadastrado.")
            else -> try {
                val id = usuarioDao.inserir(
                    Usuario(nome = nome.trim(), email = emailNormalizado, senhaHash = senha.sha256())
                )
                sessao.iniciar(id)
                Result.success(Unit)
            } catch (e: SQLiteConstraintException) {
                falha("Este e-mail já está cadastrado.")
            }
        }
    }

    suspend fun entrar(email: String, senha: String): Result<Unit> = withContext(Dispatchers.IO) {
        val emailNormalizado = email.trim().lowercase()
        if (!emailValido(emailNormalizado)) return@withContext falha("Informe um e-mail válido.")
        if (senha.isEmpty()) return@withContext falha("Informe sua senha.")

        val usuario = usuarioDao.buscarPorEmail(emailNormalizado)
        // Mensagem única: não revela se o erro foi no e-mail ou na senha.
        if (usuario == null || usuario.senhaHash != senha.sha256()) {
            falha("E-mail ou senha incorretos.")
        } else {
            sessao.iniciar(usuario.id)
            Result.success(Unit)
        }
    }

    fun sair() = sessao.encerrar()

    private fun emailValido(email: String) = Patterns.EMAIL_ADDRESS.matcher(email).matches()

    private fun falha(mensagem: String): Result<Unit> = Result.failure(Exception(mensagem))

    private companion object {
        const val TAMANHO_MINIMO_SENHA = 6
    }
}
