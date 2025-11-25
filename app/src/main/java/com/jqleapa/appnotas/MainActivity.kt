package com.jqlqapa.appnotas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.jqleapa.appnotas.ui.theme.AppNotasTheme
import com.jqlqapa.appnotas.data.AppDataContainer // Importamos el objeto Singleton
import com.jqlqapa.appnotas.ui.navigation.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppNotasTheme {
                // CORRECCIÓN:
                // En lugar de: (application as AppNotasApplication).container.noteRepository
                // Usamos directamente: AppDataContainer.noteRepository

                val repository = AppDataContainer.noteRepository

                AppNavigation(noteRepository = repository)
            }
        }
    }
}