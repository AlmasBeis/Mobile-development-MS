package kz.alfasoft.mycompany.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Indigo = Color(0xFF1E4FD8)
private val IndigoContainer = Color(0xFFDDE4FF)
private val Teal = Color(0xFF00696B)
private val TealContainer = Color(0xFFB4EDEE)

private val LightColors = lightColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    primaryContainer = IndigoContainer,
    onPrimaryContainer = Color(0xFF001552),
    secondary = Teal,
    onSecondary = Color.White,
    secondaryContainer = TealContainer,
    onSecondaryContainer = Color(0xFF002021),
    tertiary = Color(0xFF7A5900),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDEA6),
    onTertiaryContainer = Color(0xFF261900),
    background = Color(0xFFFAFAFF),
    surface = Color(0xFFFAFAFF),
    surfaceContainer = Color(0xFFEEF0F8),
    surfaceContainerHigh = Color(0xFFE8EAF2),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB5C4FF),
    onPrimary = Color(0xFF00257F),
    primaryContainer = Color(0xFF0A3AB5),
    onPrimaryContainer = IndigoContainer,
    secondary = Color(0xFF98D0D1),
    onSecondary = Color(0xFF003738),
    secondaryContainer = Color(0xFF004F51),
    onSecondaryContainer = TealContainer,
    tertiary = Color(0xFFF2BF48),
    onTertiary = Color(0xFF402D00),
    tertiaryContainer = Color(0xFF5C4200),
    onTertiaryContainer = Color(0xFFFFDEA6),
    background = Color(0xFF111318),
    surface = Color(0xFF111318),
    surfaceContainer = Color(0xFF1D2025),
    surfaceContainerHigh = Color(0xFF282A2F),
)

private val AppTypography = Typography(
    headlineSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 24.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 22.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp),
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
)

@Composable
fun MyCompanyTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
