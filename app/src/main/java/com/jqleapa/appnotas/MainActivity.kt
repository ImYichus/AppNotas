package com.jqleapa.appnotas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.jqleapa.appnotas.ui.navigation.AppNavigation
import com.jqleapa.appnotas.ui.theme.AppNotasTheme
import com.jqleapa.appnotas.ui.theme.ThemeStyle
// Estas importaciones ya no son necesarias en MainActivity:
// import com.jqlqapa.appnotas.data.AppContainer
// import com.jqlqapa.appnotas.data.AppDataContainer

// ELIMINAR ESTA LÍNEA
// lateinit var appContainerInstance: AppContainer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ELIMINAR ESTA LÍNEA
        // appContainerInstance = AppDataContainer(applicationContext)

        enableEdgeToEdge()
        setContent {
            AppNotasTheme(themeStyle = ThemeStyle.PURPLE) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }
}