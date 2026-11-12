package com.estudaai

import android.app.Application
import com.estudaai.data.AppContainer

// Cria uma única vez o banco, a sessão e os repositórios usados pelos ViewModels.
class EstudaAiApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
