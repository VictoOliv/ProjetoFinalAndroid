package com.estudaai.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Resposta da IA salva ou anotação escrita pelo próprio aluno.
// Em anotações próprias, "pergunta" guarda o título e "resposta" o texto.
// Cada anotação pertence a um usuário.
@Entity(
    tableName = "anotacoes",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("usuarioId")]
)
data class Anotacao(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioId: Long,
    val tema: String,
    val pergunta: String,
    val resposta: String,
    val criadoEm: Long = System.currentTimeMillis(),
    @ColumnInfo(defaultValue = "1") val geradaPorIa: Boolean = true
)
