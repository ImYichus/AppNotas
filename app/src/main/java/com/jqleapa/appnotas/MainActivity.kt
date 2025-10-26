package com.jqlqapa.appnotas // ¡CORREGIDO!

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.jqleapa.appnotas.ui.theme.AppNotasTheme
import com.jqleapa.appnotas.ui.theme.ThemeStyle
import com.jqlqapa.appnotas.AppNotasApplication
import com.jqlqapa.appnotas.ui.navigation.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            // Accede a la instancia del contenedor y obtiene el NoteRepository
            // (application as AppNotasApplication).container.noteRepository es una forma común en apps con DI manual.
            val noteRepository = (application as AppNotasApplication).container.noteRepository

            AppNotasTheme(themeStyle = ThemeStyle.PURPLE) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Pasa el repositorio a la navegación.
                    AppNavigation(noteRepository = noteRepository)
                }
            }
        }
    }
}
