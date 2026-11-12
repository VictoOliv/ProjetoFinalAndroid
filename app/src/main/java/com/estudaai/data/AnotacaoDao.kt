package com.estudaai.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AnotacaoDao {
    @Insert
    suspend fun inserir(anotacao: Anotacao): Long

    @Query("SELECT * FROM anotacoes WHERE usuarioId = :usuarioId ORDER BY criadoEm DESC")
    fun listarDoUsuario(usuarioId: Long): Flow<List<Anotacao>>

    @Query("SELECT * FROM anotacoes WHERE id = :id AND usuarioId = :usuarioId")
    fun buscar(id: Long, usuarioId: Long): Flow<Anotacao?>

    @Query("DELETE FROM anotacoes WHERE id = :id AND usuarioId = :usuarioId")
    suspend fun excluir(id: Long, usuarioId: Long)
}
