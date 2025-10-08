package com.jqleapa.appnotas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.jqleapa.appnotas.ui.theme.AppNotasTheme
import com.jqleapa.appnotas.ui.navigation.AppNavigation
import androidx.compose.material3.Surface
import com.jqleapa.appnotas.ui.screens.HomeScreen
import com.jqleapa.appnotas.ui.theme.AppNotasTheme
import com.jqleapa.appnotas.ui.theme.ThemeStyle

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppNotasTheme(themeStyle = ThemeStyle.EMERALD) {
                AppNavigation()
            }
        }
    }
}
