package areebah.nyuad4jetbrains.project

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

/**
 * Runs the shared Compose UI in a desktop window: `./gradlew :shared:run`.
 *
 * This exists so the UI can be looked at without waiting on an iOS simulator runtime or an Android
 * emulator. It is a development tool, not a shipping target — Android and iOS are what get built,
 * and anything that only works here does not count as done.
 *
 * The window is sized to roughly a phone so the layout is judged at the width it ships at.
 */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Desktop preview (phone-sized)",
        state = rememberWindowState(width = 420.dp, height = 900.dp),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Box(Modifier.size(width = 390.dp, height = 844.dp)) {
                App()
            }
        }
    }
}
