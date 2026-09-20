package io.github.jqssun.airplay.ui.theme

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
import io.github.jqssun.airplay.ui.isTvDevice

// Fire TV palette. Cool and desaturated on purpose: a television raises
// contrast, so warm saturated colour bands and buzzes on it.
val TvGround = Color(0xFF0B1117)
val TvSurface = Color(0xFF161E27)
val TvRaised = Color(0xFF202B36)
val TvLine = Color(0xFF2C3945)
val TvText = Color(0xFFF4F7F9)
val TvTextDim = Color(0xFFA3B1BE)
val TvAccent = Color(0xFF58B4D9)
val TvLive = Color(0xFF6FBF8B)

private val TvColorScheme = darkColorScheme(
    primary = TvAccent,
    onPrimary = TvGround,
    secondary = TvAccent,
    onSecondary = TvGround,
    tertiary = TvLive,
    onTertiary = TvGround,
    background = TvGround,
    onBackground = TvText,
    surface = TvSurface,
    onSurface = TvText,
    surfaceVariant = TvRaised,
    onSurfaceVariant = TvTextDim,
    outline = TvLine,
    outlineVariant = TvLine,
    // The navigation bar and its selected pill read these; left at the
    // Material defaults they come out lavender on a dark television.
    primaryContainer = TvRaised,
    onPrimaryContainer = TvAccent,
    secondaryContainer = TvRaised,
    onSecondaryContainer = TvAccent,
    surfaceContainerLowest = TvGround,
    surfaceContainerLow = TvGround,
    surfaceContainer = TvSurface,
    surfaceContainerHigh = TvSurface,
    surfaceContainerHighest = TvRaised,
)

@Composable
fun AirPlayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val ctx = LocalContext.current
    // A Fire TV reports night mode off, so the system-driven branch below
    // would hand a television a light lavender UI. Pin the TV palette.
    if (ctx.isTvDevice()) {
        MaterialTheme(colorScheme = TvColorScheme, content = content)
        return
    }

    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }

    MaterialTheme(colorScheme = colorScheme, content = content)
}
