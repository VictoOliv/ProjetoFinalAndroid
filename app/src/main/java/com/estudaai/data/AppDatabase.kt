package com.estudaai.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Usuario::class, Anotacao::class, RegistroEstudo::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun anotacaoDao(): AnotacaoDao
    abstract fun registroEstudoDao(): RegistroEstudoDao

    companion object {
        // Versão 2: anotações escritas pelo próprio aluno e registro de horas.
        // A migração mantém as contas e anotações de quem já tinha o app.
        private val MIGRACAO_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `anotacoes` ADD COLUMN `geradaPorIa` INTEGER NOT NULL DEFAULT 1"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `registros_estudo` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`usuarioId` INTEGER NOT NULL, " +
                        "`materia` TEXT NOT NULL, " +
                        "`minutos` INTEGER NOT NULL, " +
                        "`data` INTEGER NOT NULL, " +
                        "FOREIGN KEY(`usuarioId`) REFERENCES `usuarios`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE )"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_registros_estudo_usuarioId` " +
                        "ON `registros_estudo` (`usuarioId`)"
                )
            }
        }

        fun criar(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "estudaai.db")
                .addMigrations(MIGRACAO_1_2)
                .build()
    }
}
