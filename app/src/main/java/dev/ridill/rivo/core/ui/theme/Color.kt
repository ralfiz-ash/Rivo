package dev.ridill.rivo.core.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

val PrimaryBrandColor = Color(0xFFFFBF00)
//val SecondaryYellow = Color(0xFFFFCC33)
//val TertiaryRed = Color(0xFFFF4000)

val DarkGray = Color(0xFF121212)
val DarkGrayVariant = Color(0xFF191919)

val LightGray = Color(0xFFFAF9F6)

val md_theme_light_primary = Color(0xFF795900)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFFFDFA0)
val md_theme_light_onPrimaryContainer = Color(0xFF261A00)
val md_theme_light_secondary = Color(0xFF4C6700)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFB9F600)
val md_theme_light_onSecondaryContainer = Color(0xFF141F00)
val md_theme_light_tertiary = Color(0xFFB32A00)
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = Color(0xFFFFDBD2)
val md_theme_light_onTertiaryContainer = Color(0xFF3C0800)
val md_theme_light_error = Color(0xFFBA1A1A)
val md_theme_light_errorContainer = Color(0xFFFFDAD6)
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_onErrorContainer = Color(0xFF410002)
val md_theme_light_background = Color(0xFFFFFBFF)
val md_theme_light_onBackground = Color(0xFF1E1B16)
val md_theme_light_surface = Color(0xFFFFFBFF)
val md_theme_light_onSurface = Color(0xFF1E1B16)
val md_theme_light_surfaceVariant = Color(0xFFEDE1CF)
val md_theme_light_onSurfaceVariant = Color(0xFF4D4639)
val md_theme_light_outline = Color(0xFF7F7667)
val md_theme_light_inverseOnSurface = Color(0xFFF8EFE7)
val md_theme_light_inverseSurface = Color(0xFF34302A)
val md_theme_light_inversePrimary = Color(0xFFFBBC00)

//val md_theme_light_shadow = Color(0xFF000000)
val md_theme_light_surfaceTint = Color(0xFF795900)
val md_theme_light_outlineVariant = Color(0xFFD0C5B4)
val md_theme_light_scrim = Color(0xFF000000)

val md_theme_dark_primary = Color(0xFFFBBC00)
val md_theme_dark_onPrimary = Color(0xFF402D00)
val md_theme_dark_primaryContainer = Color(0xFF5C4300)
val md_theme_dark_onPrimaryContainer = Color(0xFFFFDFA0)
val md_theme_dark_secondary = Color(0xFFA2D800)
val md_theme_dark_onSecondary = Color(0xFF263500)
val md_theme_dark_secondaryContainer = Color(0xFF384E00)
val md_theme_dark_onSecondaryContainer = Color(0xFFB9F600)
val md_theme_dark_tertiary = Color(0xFFFFB4A1)
val md_theme_dark_onTertiary = Color(0xFF611300)
val md_theme_dark_tertiaryContainer = Color(0xFF891E00)
val md_theme_dark_onTertiaryContainer = Color(0xFFFFDBD2)
val md_theme_dark_error = Color(0xFFFFB4AB)
val md_theme_dark_errorContainer = Color(0xFF93000A)
val md_theme_dark_onError = Color(0xFF690005)
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)
val md_theme_dark_background = Color(0xFF1E1B16)
val md_theme_dark_onBackground = Color(0xFFE9E1D8)
val md_theme_dark_surface = Color(0xFF1E1B16)
val md_theme_dark_onSurface = Color(0xFFE9E1D8)
val md_theme_dark_surfaceVariant = Color(0xFF4D4639)
val md_theme_dark_onSurfaceVariant = Color(0xFFD0C5B4)
val md_theme_dark_outline = Color(0xFF998F80)
val md_theme_dark_inverseOnSurface = Color(0xFF1E1B16)
val md_theme_dark_inverseSurface = Color(0xFFE9E1D8)
val md_theme_dark_inversePrimary = Color(0xFF795900)

//val md_theme_dark_shadow = Color(0xFF000000)
val md_theme_dark_surfaceTint = Color(0xFFFBBC00)
val md_theme_dark_outlineVariant = Color(0xFF4D4639)
val md_theme_dark_scrim = Color(0xFF000000)

//val seed = Color(0xFFFFBF00)

val RivoSelectableColorsList: List<Color>
    get() = listOf(
        Color(0xFF77172E),
        Color(0xFF692C18),
        Color(0xFF7C4A03),
        Color(0xFF274D3B),
        Color(0xFF0D635D),
        Color(0xFF246377),
        Color(0xFF284255),
        Color(0xFF472E5B),
        Color(0xFF6C3A4F),
        Color(0xFF4B443A),
        Color(0xFF232427)
    ).map { it.copy(alpha = 0.64f) }


object ContentAlpha {
    const val PERCENT_32 = 0.32f
    const val PERCENT_50 = 0.50f
    const val SUB_CONTENT = 0.72f

    const val EXCLUDED = 0.50f
}

fun Color.contentColor(
    onLight: Color = Color.Black,
    onDark: Color = Color.White
): Color = if (luminance() >= 0.4f) onLight
else onDark