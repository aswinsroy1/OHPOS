package com.example.ui.theme

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font

@Immutable
data class AppColors(
    val background: Color,
    val surface: Color,
    val surfaceLighter: Color,
    val accent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val divider: Color,
    val cardLightBlue: Color,
    val cardPastelPurple: Color,
    val borderLight: Color,
    val iconDark: Color,
    val iconLight: Color,
)

val LocalAppColors = staticCompositionLocalOf {
    AppColors(
        background = Color.Unspecified,
        surface = Color.Unspecified,
        surfaceLighter = Color.Unspecified,
        accent = Color.Unspecified,
        textPrimary = Color.Unspecified,
        textSecondary = Color.Unspecified,
        divider = Color.Unspecified,
        cardLightBlue = Color.Unspecified,
        cardPastelPurple = Color.Unspecified,
        borderLight = Color.Unspecified,
        iconDark = Color.Unspecified,
        iconLight = Color.Unspecified,
    )
}

@Immutable
data class AppGradients(
    val orderCard: Brush,
    val salesCard: Brush,
    val iconContainer: Brush
)

val LocalAppGradients = staticCompositionLocalOf {
    AppGradients(
        orderCard = Brush.linearGradient(colors = listOf(Color.Transparent, Color.Transparent)),
        salesCard = Brush.linearGradient(colors = listOf(Color.Transparent, Color.Transparent)),
        iconContainer = Brush.linearGradient(colors = listOf(Color.Transparent, Color.Transparent))
    )
}

@Immutable
data class AppTypography(
    val titleLarge: TextStyle,
    val titleMedium: TextStyle,
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val labelMedium: TextStyle,
    val numberLarge: TextStyle
)

val LocalAppTypography = staticCompositionLocalOf {
    AppTypography(
        titleLarge = TextStyle.Default,
        titleMedium = TextStyle.Default,
        bodyLarge = TextStyle.Default,
        bodyMedium = TextStyle.Default,
        labelMedium = TextStyle.Default,
        numberLarge = TextStyle.Default,
    )
}

@Immutable
data class AppSpacing(
    val xxs: Dp = 4.dp,
    val xs: Dp = 8.dp,
    val sm: Dp = 12.dp,
    val md: Dp = 18.dp,
    val lg: Dp = 26.dp,
    val xl: Dp = 34.dp,
    val xxl: Dp = 52.dp,
)
val LocalAppSpacing = staticCompositionLocalOf { AppSpacing() }

@Immutable
data class AppRadius(
    val sm: RoundedCornerShape = RoundedCornerShape(12.dp),
    val md: RoundedCornerShape = RoundedCornerShape(16.dp),
    val lg: RoundedCornerShape = RoundedCornerShape(24.dp),
    val xl: RoundedCornerShape = RoundedCornerShape(24.dp),
    val full: RoundedCornerShape = RoundedCornerShape(100)
)
val LocalAppRadius = staticCompositionLocalOf { AppRadius() }

@Immutable
data class AppElevations(
    val none: Dp = 0.dp,
    val sm: Dp = 4.dp,
    val md: Dp = 8.dp,
    val lg: Dp = 16.dp,
)
val LocalAppElevations = staticCompositionLocalOf { AppElevations() }

@Immutable
data class AppDimensions(
    val navBarHeight: Dp = 84.dp,
    val navIconContainerSize: Dp = 64.dp,
    val navIconInnerSize: Dp = 42.dp,
    val navIconSize: Dp = 24.dp,
    val topBarHeight: Dp = 64.dp,
    val buttonHeight: Dp = 56.dp,
    val listItemIconSize: Dp = 48.dp,
    val iconSizeMd: Dp = 24.dp,
    val iconSizeSm: Dp = 22.dp,
    val borderThickness: Dp = 1.dp
)
val LocalAppDimensions = staticCompositionLocalOf { AppDimensions() }

object AppTheme {
    val colors: AppColors
        @Composable get() = LocalAppColors.current
    val gradients: AppGradients
        @Composable get() = LocalAppGradients.current
    val typography: AppTypography
        @Composable get() = LocalAppTypography.current
    val spacing: AppSpacing
        @Composable get() = LocalAppSpacing.current
    val radius: AppRadius
        @Composable get() = LocalAppRadius.current
    val elevations: AppElevations
        @Composable get() = LocalAppElevations.current
    val dimensions: AppDimensions
        @Composable get() = LocalAppDimensions.current
}

// Google Fonts Provider
val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = com.example.R.array.com_google_android_gms_fonts_certs
)
val GeometricFontName = GoogleFont("Google Sans")
val GeometricFontFamily = FontFamily(
    Font(googleFont = GeometricFontName, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GeometricFontName, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GeometricFontName, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = GeometricFontName, fontProvider = provider, weight = FontWeight.SemiBold)
)

val BackgroundDark = Color(0xFF1B1B1D)
val SurfaceDark = Color(0xFF252528)
val SurfaceLighter = Color(0xFF2A2A2D)
val AccentPurple = Color(0xFFDCC8FF)
val CardLightBlue = Color(0xFFE2FAFF)
val CardPastelPurple = Color(0xFFEBDCFF)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFB8B8B8)
val DividerColor = Color(0xFFFFFFFF).copy(alpha = 0.03f)

val defaultTypography = AppTypography(
    titleLarge = TextStyle(
        fontFamily = GeometricFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    titleMedium = TextStyle(
        fontFamily = GeometricFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = GeometricFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = GeometricFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = GeometricFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    numberLarge = TextStyle(
        fontFamily = GeometricFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    )
)

val darkColors = AppColors(
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceLighter = SurfaceLighter,
    accent = AccentPurple,
    textPrimary = TextPrimary,
    textSecondary = TextSecondary,
    divider = DividerColor,
    cardLightBlue = CardLightBlue,
    cardPastelPurple = CardPastelPurple,
    borderLight = Color(0xFF4C4C4E).copy(alpha = 0.5f),
    iconDark = Color(0xFF1B1B1D),
    iconLight = Color.White
)

val pureBlackColors = AppColors(
    background = Color(0xFF000000),
    surface = Color(0xFF0A0A0A),
    surfaceLighter = Color(0xFF141414),
    accent = AccentPurple,
    textPrimary = TextPrimary,
    textSecondary = TextSecondary,
    divider = DividerColor,
    cardLightBlue = CardLightBlue,
    cardPastelPurple = CardPastelPurple,
    borderLight = Color(0xFF2A2A2A),
    iconDark = Color(0xFF000000),
    iconLight = Color.White
)

val lightColors = AppColors(
    background = Color(0xFFF7F7F7),
    surface = Color(0xFFFFFFFF),
    surfaceLighter = Color(0xFFF0F0F0),
    accent = AccentPurple,
    textPrimary = Color(0xFF1B1B1D),
    textSecondary = Color(0xFF6E6E6E),
    divider = Color(0xFF000000).copy(alpha = 0.05f),
    cardLightBlue = CardLightBlue,
    cardPastelPurple = CardPastelPurple,
    borderLight = Color(0xFFE0E0E0),
    iconDark = Color(0xFF1B1B1D),
    iconLight = Color.White
)

val appGradients = AppGradients(
    orderCard = Brush.linearGradient(colors = listOf(Color(0xFFF0E5FF), Color(0xFFE5D4FF))),
    salesCard = Brush.linearGradient(colors = listOf(Color(0xFFE5D4FF), Color(0xFFDCC8FF))),
    iconContainer = Brush.linearGradient(colors = listOf(Color(0xFF2C2C2E), Color(0xFF1C1C1E)))
)

@Composable
fun AppDesignSystem(
    content: @Composable () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val appearanceRepo = androidx.compose.runtime.remember { com.example.data.AppearancePreferencesRepository(context) }
    
    val themePref by appearanceRepo.themeFlow.collectAsState(initial = "Dark")
    val accentColourPref by appearanceRepo.accentColourFlow.collectAsState(initial = "Monochrome")
    
    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
    
    val isDarkTheme = when (themePref) {
        "Light" -> false
        "System Default" -> isSystemDark
        else -> true // Dark
    }
    
    val accentColor = when (accentColourPref) {
        "Graphite" -> if (isDarkTheme) Color(0xFF9B9B9E) else Color(0xFF4A4A4D)
        "Slate" -> if (isDarkTheme) Color(0xFF9DB0BE) else Color(0xFF5C6773)
        "Amber Brass" -> Color(0xFFB8863B)
        "Sage" -> Color(0xFF7C8C6E)
        "Ink" -> if (isDarkTheme) Color(0xFF7C8FA8) else Color(0xFF3C4858)
        else -> if (isDarkTheme) Color(0xFFFFFFFF) else Color(0xFF1B1B1D) // Monochrome
    }
    
    val colors = when (themePref) {
        "Light" -> lightColors.copy(accent = accentColor)
        "Pure Black" -> pureBlackColors.copy(accent = accentColor)
        "System Default" -> if (isSystemDark) darkColors.copy(accent = accentColor) else lightColors.copy(accent = accentColor)
        else -> darkColors.copy(accent = accentColor)
    }

    CompositionLocalProvider(
        LocalAppColors provides colors,
        LocalAppGradients provides appGradients,
        LocalAppTypography provides defaultTypography,
        LocalAppSpacing provides AppSpacing(),
        LocalAppRadius provides AppRadius(),
        LocalAppElevations provides AppElevations(),
        LocalAppDimensions provides AppDimensions(),
        content = content
    )
}
