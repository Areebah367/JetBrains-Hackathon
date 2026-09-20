package areebah.nyuad4jetbrains.project.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Line icons built from SVG path data, taken from the HTML design prototype in `docs/design`.
 *
 * Compose's own icon packs are a separate dependency; `PathParser` ships with compose-ui, so this
 * keeps the icons without adding one. They are stroked, not filled, to match the prototype.
 */
private fun lineIcon(name: String, pathData: String): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).addPath(
        pathData = PathParser().parsePathString(pathData).toNodes(),
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
    ).build()

object AppIcons {
    val Interests: ImageVector = lineIcon(
        "Interests",
        "M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.7l-1-1.1a5.5 5.5 0 0 0-7.8 7.8L12 21l8.8-8.6a5.5 5.5 0 0 0 0-7.8z",
    )

    val WhatsOn: ImageVector = lineIcon(
        "WhatsOn",
        "M3 7a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v3a2 2 0 0 0 0 4v3a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-3a2 2 0 0 0 0-4zM14 5v2M14 11v2M14 17v2",
    )

    val Plan: ImageVector = lineIcon("Plan", "M3 5h18v16H3zM16 3v4M8 3v4M3 10h18")
}
