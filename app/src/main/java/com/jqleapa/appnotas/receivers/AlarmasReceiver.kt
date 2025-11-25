package com.jqlqapa.appnotas.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.jqlqapa.appnotas.R // Asegúrate de tener un icono de launcher o cambiarlo

class AlarmasReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra("EXTRA_REMINDER_ID", -1L) // Usaremos el ID
        val message = intent.getStringExtra("EXTRA_MESSAGE") ?: "Recordatorio sin título"

        Log.d("AlarmasReceiver", "Alarma recibida - ID: $reminderId, Mensaje: $message")

        if (reminderId == -1L) return

        val channelId = "reminder_channel_id" // El ID que definiste en AlarmApp.kt

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // ⚠️ Reemplazar con el icono de tu app
            .setContentTitle("Recordatorio de Nota")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) // Se cierra al tocarla

        // Usamos el ID del recordatorio como ID de la notificación para que sean únicos
        notificationManager.notify(reminderId.toInt(), builder.build())

        Log.d("AlarmasReceiver", "Notificación enviada con ID: $reminderId")

        // OPCIONAL: Aquí podrías querer eliminar el recordatorio de la DB si es de una sola vez
    }
}