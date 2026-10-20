package cinema.booking.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF10131A)
private val Amber = Color(0xFFE4B15A)
private val AmberContainer = Color(0xFF5C4314)
private val OnAmber = Color(0xFF3D2A00)
private val Mist = Color(0xFFE7E4DC)
private val Sea = Color(0xFF9FCBD8)

private val CinemaColors = darkColorScheme(
    primary = Amber,
    onPrimary = OnAmber,
    primaryContainer = AmberContainer,
    onPrimaryContainer = Color(0xFFFFE3AE),
    secondary = Sea,
    onSecondary = Color(0xFF003640),
    secondaryContainer = Color(0xFF204852),
    onSecondaryContainer = Color(0xFFC5EAF4),
    background = Ink,
    onBackground = Mist,
    surface = Ink,
    onSurface = Mist,
    surfaceVariant = Color(0xFF33384A),
    onSurfaceVariant = Color(0xFFC8C6C0),
    surfaceContainer = Color(0xFF1A1F2B),
    surfaceContainerHigh = Color(0xFF222838),
    surfaceContainerHighest = Color(0xFF2C3344),
    outline = Color(0xFF918F88),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

private val CinemaTypography = Typography(
    headlineSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 24.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 22.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp),
)

private val CinemaShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
)

@Composable
fun CinemaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CinemaColors,
        typography = CinemaTypography,
        shapes = CinemaShapes,
        content = content,
    )
}
