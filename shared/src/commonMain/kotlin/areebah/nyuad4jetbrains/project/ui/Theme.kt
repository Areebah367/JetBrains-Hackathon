package areebah.nyuad4jetbrains.project.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Whether the app follows the device, or has been forced one way from inside the app. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Warm sand and deep teal — legible in both schemes, and not the default purple. */
private val lightScheme = lightColorScheme(
    primary = Color(0xFF00696E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF9CF0F6),
    onPrimaryContainer = Color(0xFF002022),
    secondary = Color(0xFF4A6365),
    secondaryContainer = Color(0xFFCCE8EA),
    onSecondaryContainer = Color(0xFF051F21),
    tertiary = Color(0xFF8A5100),
    tertiaryContainer = Color(0xFFFFDCBE),
    onTertiaryContainer = Color(0xFF2C1600),
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFAFDFC),
    onBackground = Color(0xFF191C1C),
    surface = Color(0xFFFAFDFC),
    onSurface = Color(0xFF191C1C),
    surfaceVariant = Color(0xFFDAE4E5),
    onSurfaceVariant = Color(0xFF3F4849),
    outlineVariant = Color(0xFFBEC8C9),
)

private val darkScheme = darkColorScheme(
    primary = Color(0xFF80D4DA),
    onPrimary = Color(0xFF003739),
    primaryContainer = Color(0xFF004F53),
    onPrimaryContainer = Color(0xFF9CF0F6),
    secondary = Color(0xFFB0CCCE),
    secondaryContainer = Color(0xFF324B4D),
    onSecondaryContainer = Color(0xFFCCE8EA),
    tertiary = Color(0xFFFFB871),
    tertiaryContainer = Color(0xFF683D00),
    onTertiaryContainer = Color(0xFFFFDCBE),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF101414),
    onBackground = Color(0xFFE0E3E3),
    surface = Color(0xFF101414),
    onSurface = Color(0xFFE0E3E3),
    surfaceVariant = Color(0xFF3F4849),
    onSurfaceVariant = Color(0xFFBEC8C9),
    outlineVariant = Color(0xFF3F4849),
)

@Composable
fun AppTheme(mode: ThemeMode = ThemeMode.SYSTEM, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    MaterialTheme(
        colorScheme = if (dark) darkScheme else lightScheme,
        content = content,
    )
}
