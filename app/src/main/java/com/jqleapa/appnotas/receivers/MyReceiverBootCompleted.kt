package com.jqlqapa.appnotas.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class MyReceiverBootCompleted : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Sistema cargado. Reprogramando alarmas pendientes...")
            // TODO: Lógica para leer TODOS los recordatorios de la base de datos
            //       y volver a llamar a alarmScheduler.schedule(item) para cada uno.
        }
    }
}