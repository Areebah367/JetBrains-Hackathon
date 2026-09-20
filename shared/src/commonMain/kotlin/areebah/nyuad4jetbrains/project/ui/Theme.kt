package areebah.nyuad4jetbrains.project.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Whether the app follows the device, or has been forced one way from inside the app. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Deep violet with a warm orange accent, after the reference design. Defined for both schemes. */
private val lightScheme = lightColorScheme(
    primary = Color(0xFF5B3FBF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE7DEFF),
    onPrimaryContainer = Color(0xFF1B0364),
    secondary = Color(0xFF615A76),
    secondaryContainer = Color(0xFFE7DEFF),
    onSecondaryContainer = Color(0xFF1D1830),
    tertiary = Color(0xFFE2571F),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDBCE),
    onTertiaryContainer = Color(0xFF3A0B00),
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFFFBFF),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE6E0EC),
    onSurfaceVariant = Color(0xFF48454E),
    outlineVariant = Color(0xFFC9C5D0),
)

private val darkScheme = darkColorScheme(
    primary = Color(0xFFC9BFFF),
    onPrimary = Color(0xFF301D75),
    primaryContainer = Color(0xFF47338D),
    onPrimaryContainer = Color(0xFFE7DEFF),
    secondary = Color(0xFFCBC2DB),
    secondaryContainer = Color(0xFF494458),
    onSecondaryContainer = Color(0xFFE7DEFF),
    tertiary = Color(0xFFFF8C5A),
    onTertiary = Color(0xFF5C1A00),
    tertiaryContainer = Color(0xFFB03D0C),
    onTertiaryContainer = Color(0xFFFFDBCE),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    // A deep indigo rather than neutral grey, so the dark theme reads as the reference does.
    background = Color(0xFF15121E),
    onBackground = Color(0xFFE6E0EC),
    surface = Color(0xFF15121E),
    onSurface = Color(0xFFE6E0EC),
    surfaceVariant = Color(0xFF2A2634),
    onSurfaceVariant = Color(0xFFC9C5D0),
    outlineVariant = Color(0xFF3A3545),
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
