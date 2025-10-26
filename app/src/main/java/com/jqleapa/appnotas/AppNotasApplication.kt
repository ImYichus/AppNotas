package com.jqlqapa.appnotas

import android.app.Application
import com.jqlqapa.appnotas.data.AppContainer
import com.jqlqapa.appnotas.data.AppDataContainer

class AppNotasApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        // Inicializa el contenedor con el contexto de aplicación
        AppDataContainer.initialize(this)
        container = AppDataContainer
    }
}
