package br.com.fiap.aguiaexpress.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AguiaRed,
    onPrimary = AguiaWhite,
    primaryContainer = AguiaDarkRed,
    onPrimaryContainer = AguiaWhite,

    secondary = AguiaLightGrey,
    onSecondary = AguiaBlack,
    secondaryContainer = AguiaDarkGrey,
    onSecondaryContainer = AguiaWhite,

    tertiary = AguiaGrey,
    onTertiary = AguiaWhite,

    background = AguiaBlack,
    onBackground = AguiaWhite,

    surface = AguiaDarkGrey,
    onSurface = AguiaWhite,

    surfaceVariant = Color(0xFF3A3A3A),
    onSurfaceVariant = Color(0xFFE0E0E0),

    outline = Color(0xFF9E9E9E),

    error = Color(0xFFFF5252),
    onError = AguiaWhite
)

private val LightColorScheme = lightColorScheme(
    primary = AguiaRed,
    onPrimary = AguiaWhite,
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = Color(0xFF410002),

    secondary = AguiaDarkGrey,
    onSecondary = AguiaWhite,
    secondaryContainer = AguiaLightGrey,
    onSecondaryContainer = AguiaBlack,

    tertiary = AguiaGrey,
    onTertiary = AguiaWhite,

    background = AguiaBackground,
    onBackground = AguiaBlack,

    surface = AguiaWhite,
    onSurface = AguiaBlack,

    surfaceVariant = Color(0xFFF1F1F1),
    onSurfaceVariant = AguiaDarkGrey,

    outline = Color(0xFFBDBDBD),

    error = AguiaRed,
    onError = AguiaWhite
)

@Composable
fun AguiaExpressTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}