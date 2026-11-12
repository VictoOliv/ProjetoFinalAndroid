package com.estudaai.data

import android.content.Context
import com.estudaai.network.RetrofitInstance

// Monta as dependências do app em um só lugar.
class AppContainer(context: Context) {
    private val database = AppDatabase.criar(context)
    private val sessao = SessaoManager(context)

    val usuarioRepository = UsuarioRepository(database.usuarioDao(), sessao)
    val estudoRepository = EstudoRepository(RetrofitInstance.api, database.anotacaoDao(), sessao)
    val horasEstudoRepository = HorasEstudoRepository(database.registroEstudoDao(), sessao)
}
