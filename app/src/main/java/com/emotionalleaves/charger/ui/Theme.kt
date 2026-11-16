package com.emotionalleaves.charger.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Calm ink-blue and warm paper. Quiet on purpose: one accent, lots of space.
private val Ink = Color(0xFF1F3A5F)
private val InkSoft = Color(0xFFDCE6F2)
private val Paper = Color(0xFFF8F6F2)
private val Card = Color(0xFFFFFFFF)
private val Line = Color(0xFFE3DFD7)
private val Text = Color(0xFF1B1C1E)
private val TextSoft = Color(0xFF5B5F66)

private val light = lightColorScheme(
    primary = Ink, onPrimary = Color.White,
    primaryContainer = InkSoft, onPrimaryContainer = Color(0xFF0E2340),
    secondary = Color(0xFF4F6B5A), onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDEBDF), onSecondaryContainer = Color(0xFF15301F),
    tertiary = Color(0xFF8A5A14), tertiaryContainer = Color(0xFFF7E6C8), onTertiaryContainer = Color(0xFF3A2500),
    error = Color(0xFFB3261E), errorContainer = Color(0xFFF9DEDC), onErrorContainer = Color(0xFF410E0B),
    background = Paper, onBackground = Text,
    surface = Card, onSurface = Text,
    surfaceVariant = Color(0xFFF0EDE7), onSurfaceVariant = TextSoft,
    surfaceContainerLowest = Card, surfaceContainerLow = Card, surfaceContainer = Color(0xFFF3F0EA),
    surfaceContainerHigh = Color(0xFFEFEBE4), surfaceContainerHighest = Color(0xFFEAE6DE),
    outline = Color(0xFFB9B4AA), outlineVariant = Line,
)

private val dark = darkColorScheme(
    primary = Color(0xFFAFC6E6), onPrimary = Color(0xFF0E2340),
    primaryContainer = Color(0xFF2B4466), onPrimaryContainer = Color(0xFFDCE6F2),
    secondary = Color(0xFFB4CDBB), secondaryContainer = Color(0xFF34503F), onSecondaryContainer = Color(0xFFDDEBDF),
    tertiary = Color(0xFFE8C489), tertiaryContainer = Color(0xFF5C3F0E), onTertiaryContainer = Color(0xFFF7E6C8),
    background = Color(0xFF141517), onBackground = Color(0xFFE6E3DE),
    surface = Color(0xFF1C1D20), onSurface = Color(0xFFE6E3DE),
    surfaceVariant = Color(0xFF26282C), onSurfaceVariant = Color(0xFFB8B4AD),
    surfaceContainerLowest = Color(0xFF1C1D20), surfaceContainerLow = Color(0xFF1C1D20), surfaceContainer = Color(0xFF222326),
    surfaceContainerHigh = Color(0xFF292A2E), surfaceContainerHighest = Color(0xFF303135),
    outline = Color(0xFF6E6A63), outlineVariant = Color(0xFF34353A),
)

private val type = Typography(
    headlineMedium = TextStyle(fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.SemiBold),
    headlineSmall = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 19.sp, lineHeight = 25.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
    labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.4.sp),
)

private val shapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
)

@Composable
fun ChargerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) dark else light,
        typography = type,
        shapes = shapes,
        content = content,
    )
}
