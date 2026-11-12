package com.estudaai.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RegistroEstudoDao {
    @Insert
    suspend fun inserir(registro: RegistroEstudo): Long

    @Query("SELECT * FROM registros_estudo WHERE usuarioId = :usuarioId ORDER BY data DESC")
    fun listarDoUsuario(usuarioId: Long): Flow<List<RegistroEstudo>>

    @Query("DELETE FROM registros_estudo WHERE id = :id AND usuarioId = :usuarioId")
    suspend fun excluir(id: Long, usuarioId: Long)
}
