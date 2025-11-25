package com.jqleapa.appnotas.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

enum class ThemeStyle {
    PURPLE,
    EMERALD
}


private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF1C1B1F),
    onPrimary = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White
)


private val EmeraldDarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    secondary = EmeraldSecondary,
    tertiary = EmeraldTertiary,
    background = DarkSurface,
    surface = DarkSurface,
    onPrimary = Color.White
)

private val EmeraldLightColorScheme = lightColorScheme(
    primary = EmeraldLightPrimary,
    secondary = EmeraldLightSecondary,
    tertiary = EmeraldLightTertiary,
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSurface = EmeraldLightOnSurface
)


@Composable
fun AppNotasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),

    themeStyle: ThemeStyle = ThemeStyle.PURPLE,

    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val selectedColorScheme = when (themeStyle) {
        ThemeStyle.EMERALD -> if (darkTheme) EmeraldDarkColorScheme else EmeraldLightColorScheme
        ThemeStyle.PURPLE -> {

            if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {

                if (darkTheme) DarkColorScheme else LightColorScheme
            }
        }
    }


    MaterialTheme(
        colorScheme = selectedColorScheme,
        typography = Typography,
        content = content
    )
}
