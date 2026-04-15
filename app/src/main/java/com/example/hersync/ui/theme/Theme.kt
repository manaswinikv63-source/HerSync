package com.example.hersync.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val HerSyncDarkScheme = darkColorScheme(
    primary = AccentPink,
    onPrimary = Color.White,
    primaryContainer = AccentPinkMuted,
    secondary = FertileMint,
    onSecondary = Color.White,
    tertiary = FollicularPurple,
    background = Charcoal,
    surface = SurfaceCard,
    surfaceVariant = SurfaceCardElevated,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextMuted,
    outline = Color(0xFF5A5A62),
)

@Composable
fun HerSyncTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = HerSyncDarkScheme,
        typography = Typography,
        content = content,
    )
}
