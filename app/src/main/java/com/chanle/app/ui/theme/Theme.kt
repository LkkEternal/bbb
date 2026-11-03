package com.chanle.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Color(0xFFC8553D),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD2),
    onPrimaryContainer = Color(0xFF3F0B03),
    secondary = Color(0xFF7A5A3A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3E0CC),
    onSecondaryContainer = Color(0xFF2B1B0C),
    tertiary = Color(0xFF4F6B4A),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD3E8CD),
    onTertiaryContainer = Color(0xFF10230E),
    background = Color(0xFFFFF8F3),
    onBackground = Color(0xFF221A16),
    surface = Color(0xFFFFFBF8),
    onSurface = Color(0xFF221A16),
    surfaceVariant = Color(0xFFF3E4DA),
    onSurfaceVariant = Color(0xFF53443B),
    outline = Color(0xFF8C7A6E),
    error = Color(0xFFBA1A1A),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB4A2),
    onPrimary = Color(0xFF5F1507),
    primaryContainer = Color(0xFF7D2C1A),
    onPrimaryContainer = Color(0xFFFFDAD2),
    secondary = Color(0xFFE5C39A),
    onSecondary = Color(0xFF432C12),
    secondaryContainer = Color(0xFF5C4226),
    onSecondaryContainer = Color(0xFFF3E0CC),
    tertiary = Color(0xFFB7CDB1),
    onTertiary = Color(0xFF233421),
    tertiaryContainer = Color(0xFF394B36),
    onTertiaryContainer = Color(0xFFD3E8CD),
    background = Color(0xFF1A1310),
    onBackground = Color(0xFFF0E0D8),
    surface = Color(0xFF1A1310),
    onSurface = Color(0xFFF0E0D8),
    surfaceVariant = Color(0xFF4A3B34),
    onSurfaceVariant = Color(0xFFD8C2B8),
    outline = Color(0xFFA08C80),
    error = Color(0xFFFFB4AB),
)

private val ChanLeShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun ChanLeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        shapes = ChanLeShapes,
        content = content,
    )
}
