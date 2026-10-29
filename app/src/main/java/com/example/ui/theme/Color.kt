package com.example.ui.theme

import androidx.compose.ui.graphics.Color

enum class SssNeonTheme(
    val id: String,
    val arabicName: String,
    val englishBadge: String,
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val primaryNeon: Color,
    val secondaryNeon: Color,
    val tertiaryNeon: Color,
    val onBackground: Color,
    val onSurfaceMuted: Color
) {
    CYBER_CYAN(
        id = "CYBER_CYAN",
        arabicName = "سيان سايبر (Cyber Cyan)",
        englishBadge = "NEON 01",
        background = Color(0xFF060812),
        surface = Color(0xFF0E1324),
        surfaceElevated = Color(0xFF161E36),
        primaryNeon = Color(0xFF00F5D4),
        secondaryNeon = Color(0xFF00BBF9),
        tertiaryNeon = Color(0xFF7B2CBF),
        onBackground = Color(0xFFEAFBF8),
        onSurfaceMuted = Color(0xFF8CA2C0)
    ),
    NEON_MAGENTA(
        id = "NEON_MAGENTA",
        arabicName = "ماجنتا نيون (Neon Magenta)",
        englishBadge = "NEON 02",
        background = Color(0xFF0B0510),
        surface = Color(0xFF170B22),
        surfaceElevated = Color(0xFF241136),
        primaryNeon = Color(0xFFFF2A85),
        secondaryNeon = Color(0xFFFF70A6),
        tertiaryNeon = Color(0xFF00F5D4),
        onBackground = Color(0xFFFFF0F6),
        onSurfaceMuted = Color(0xFFB896B8)
    ),
    ELECTRIC_VIOLET(
        id = "ELECTRIC_VIOLET",
        arabicName = "بنفسجي كهربائي (Electric Violet)",
        englishBadge = "NEON 03",
        background = Color(0xFF070412),
        surface = Color(0xFF120B29),
        surfaceElevated = Color(0xFF1D1340),
        primaryNeon = Color(0xFFB5179E),
        secondaryNeon = Color(0xFF9D4EDD),
        tertiaryNeon = Color(0xFF4CC9F0),
        onBackground = Color(0xFFF3EBFF),
        onSurfaceMuted = Color(0xFFA092C4)
    ),
    TOXIC_EMERALD(
        id = "TOXIC_EMERALD",
        arabicName = "زمردي ماتريكس (Matrix Emerald)",
        englishBadge = "NEON 04",
        background = Color(0xFF040C08),
        surface = Color(0xFF0B1C14),
        surfaceElevated = Color(0xFF122C20),
        primaryNeon = Color(0xFF00FF87),
        secondaryNeon = Color(0xFF60EFFF),
        tertiaryNeon = Color(0xFFADFF02),
        onBackground = Color(0xFFE9FFF3),
        onSurfaceMuted = Color(0xFF83B39B)
    ),
    SOLAR_AMBER(
        id = "SOLAR_AMBER",
        arabicName = "كهرمان شمسي (Solar Amber)",
        englishBadge = "NEON 05",
        background = Color(0xFF0E0904),
        surface = Color(0xFF1C1308),
        surfaceElevated = Color(0xFF2B1D0D),
        primaryNeon = Color(0xFFFFB703),
        secondaryNeon = Color(0xFFFB8500),
        tertiaryNeon = Color(0xFFFF006E),
        onBackground = Color(0xFFFFF8E8),
        onSurfaceMuted = Color(0xFFC4A77D)
    ),
    CRIMSON_LASER(
        id = "CRIMSON_LASER",
        arabicName = "ليزر قرمزي (Crimson Laser)",
        englishBadge = "NEON 06",
        background = Color(0xFF0F0407),
        surface = Color(0xFF1F0910),
        surfaceElevated = Color(0xFF300E19),
        primaryNeon = Color(0xFFFF1E56),
        secondaryNeon = Color(0xFFFFAC41),
        tertiaryNeon = Color(0xFF00F5D4),
        onBackground = Color(0xFFFFEDF1),
        onSurfaceMuted = Color(0xFFC28C9A)
    );

    companion object {
        fun fromId(id: String): SssNeonTheme {
            return entries.find { it.id == id } ?: CYBER_CYAN
        }
    }
}
