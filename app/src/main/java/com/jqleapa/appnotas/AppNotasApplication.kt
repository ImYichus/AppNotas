package com.jqlqapa.appnotas

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.jqlqapa.appnotas.data.AppDataContainer // Asegúrate de importar tu contenedor

class AppNotasApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 1. Inicializar el Contenedor de Dependencias (Base de datos, Repositorios)
        AppDataContainer.initialize(this)

        // 2. Crear el Canal de Notificaciones (Lógica de Alarmas)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "reminder_channel_id" // Debe coincidir con el ID en AlarmasReceiver
            val channelName = "Recordatorios de Notas"
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal para notificaciones de recordatorios de notas y tareas."
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}