package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ui.theme.LocalSssVisualConfig
import com.example.ui.theme.OrbitronFontFamily

data class SssNavDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)

/**
 * Renders an icon dynamically styled according to the user's chosen `iconStyle`:
 * - NEON_GLOW: Rounded container with glowing neon halo border
 * - MINIMAL_OUTLINE: Clean thin geometric outline frame
 * - SOLID_CYBER: Cut-corner cyberpunk badge with high-contrast fill
 * - DUOTONE_RING: Double-ring circular neon orbital frame
 */
@Composable
fun SssStyledIcon(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    sizeDp: Dp = 40.dp,
    tintOverride: Color? = null
) {
    val config = LocalSssVisualConfig.current
    val palette = config.neonTheme
    val glowAlpha = (config.neonGlowIntensity / 100f).coerceIn(0.1f, 1f)
    val primary = tintOverride ?: palette.primaryNeon
    val secondary = palette.secondaryNeon

    when (config.iconStyle) {
        "MINIMAL_OUTLINE" -> {
            Box(
                modifier = modifier
                    .size(sizeDp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(
                        width = if (isSelected) 1.8.dp else 1.dp,
                        color = if (isSelected) primary else palette.onSurfaceMuted.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = if (isSelected) primary else palette.onBackground.copy(alpha = 0.8f),
                    modifier = Modifier.size(sizeDp * 0.52f)
                )
            }
        }

        "SOLID_CYBER" -> {
            val cyberShape = CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp)
            Box(
                modifier = modifier
                    .size(sizeDp)
                    .clip(cyberShape)
                    .background(
                        if (isSelected) primary else palette.surfaceElevated
                    )
                    .border(
                        width = 1.5.dp,
                        color = if (isSelected) secondary else primary.copy(alpha = 0.45f),
                        shape = cyberShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = if (isSelected) Color(0xFF05070E) else primary,
                    modifier = Modifier.size(sizeDp * 0.52f)
                )
            }
        }

        "DUOTONE_RING" -> {
            Box(
                modifier = modifier
                    .size(sizeDp)
                    .clip(CircleShape)
                    .background(
                        Brush.sweepGradient(
                            listOf(
                                primary.copy(alpha = if (isSelected) 0.32f else 0.14f),
                                secondary.copy(alpha = if (isSelected) 0.32f else 0.14f),
                                primary.copy(alpha = if (isSelected) 0.32f else 0.14f)
                            )
                        )
                    )
                    .border(
                        width = if (isSelected) 2.dp else 1.2.dp,
                        brush = Brush.linearGradient(listOf(primary, secondary)),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = if (isSelected) primary else secondary,
                    modifier = Modifier.size(sizeDp * 0.5f)
                )
            }
        }

        else -> {
            // Default: NEON_GLOW
            Box(
                modifier = modifier
                    .size(sizeDp)
                    .drawBehind {
                        if (isSelected || glowAlpha > 0.3f) {
                            drawRoundRect(
                                color = primary.copy(alpha = if (isSelected) 0.35f * glowAlpha else 0.14f * glowAlpha),
                                cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
                            )
                        }
                    }
                    .clip(RoundedCornerShape(13.dp))
                    .background(
                        if (isSelected) primary.copy(alpha = 0.22f) else palette.surfaceElevated.copy(alpha = 0.85f)
                    )
                    .border(
                        width = if (isSelected) 1.8.dp else 1.dp,
                        color = if (isSelected) primary else primary.copy(alpha = 0.35f * glowAlpha),
                        shape = RoundedCornerShape(13.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = if (isSelected) primary else palette.onBackground,
                    modifier = Modifier.size(sizeDp * 0.54f)
                )
            }
        }
    }
}

/**
 * Custom Canvas + Coil artwork renderer for Manga covers so every manga card has striking
 * cyberpunk neon artwork whether loaded from MangaDex or rendered offline.
 */
@Composable
fun SssMangaCoverArt(
    title: String,
    genre: String,
    coverUrl: String,
    accentHex: Long,
    modifier: Modifier = Modifier
) {
    val config = LocalSssVisualConfig.current
    val accentColor = Color(accentHex)
    val secondaryColor = config.neonTheme.secondaryNeon

    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0D1A),
                        accentColor.copy(alpha = 0.28f),
                        Color(0xFF060811)
                    )
                )
            )
    ) {
        // Stylized geometric manga cover illustration in Canvas underneath
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Diagonal neon slash lines
            drawLine(
                color = accentColor.copy(alpha = 0.35f),
                start = Offset(0f, h * 0.25f),
                end = Offset(w, h * 0.05f),
                strokeWidth = 3f
            )
            drawLine(
                color = secondaryColor.copy(alpha = 0.25f),
                start = Offset(0f, h * 0.75f),
                end = Offset(w, h * 0.45f),
                strokeWidth = 2f
            )

            // Glowing cyber sun/halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accentColor.copy(alpha = 0.55f), Color.Transparent),
                    center = Offset(w * 0.5f, h * 0.38f),
                    radius = w * 0.42f
                ),
                radius = w * 0.42f,
                center = Offset(w * 0.5f, h * 0.38f)
            )

            // Stylized mecha/samurai crest silhouette
            val crest = Path().apply {
                moveTo(w * 0.5f, h * 0.18f)
                lineTo(w * 0.72f, h * 0.42f)
                lineTo(w * 0.58f, h * 0.62f)
                lineTo(w * 0.42f, h * 0.62f)
                lineTo(w * 0.28f, h * 0.42f)
                close()
            }
            drawPath(
                path = crest,
                color = accentColor.copy(alpha = 0.24f)
            )
            drawPath(
                path = crest,
                color = accentColor.copy(alpha = 0.8f),
                style = Stroke(width = 2.5f)
            )
        }

        if (coverUrl.isNotBlank()) {
            AsyncImage(
                model = coverUrl,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Top genre neon pill & bottom gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x9905070E),
                            Color(0xEE05070E)
                        ),
                        startY = 80f
                    )
                )
                .padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xCC060812))
                    .border(1.dp, accentColor.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = genre.uppercase(),
                    fontFamily = OrbitronFontFamily,
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                    maxLines = 1
                )
            }

            Text(
                text = "SSS",
                fontFamily = OrbitronFontFamily,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.65f),
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    }
}

@Composable
fun SssAdaptiveBottomBar(
    destinations: List<SssNavDestination>,
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val config = LocalSssVisualConfig.current
    val palette = config.neonTheme
    val glowAlpha = (config.neonGlowIntensity / 100f).coerceIn(0.15f, 1f)

    if (config.navigationBarStyle == "FLOATING_PILL") {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                palette.surface.copy(alpha = 0.96f),
                                palette.surfaceElevated.copy(alpha = 0.96f),
                                palette.surface.copy(alpha = 0.96f)
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.horizontalGradient(
                            listOf(
                                palette.primaryNeon.copy(alpha = 0.75f * glowAlpha),
                                palette.secondaryNeon.copy(alpha = 0.75f * glowAlpha)
                            )
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                destinations.forEach { dest ->
                    val selected = currentRoute == dest.route
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (selected) palette.primaryNeon.copy(alpha = 0.16f) else Color.Transparent
                            )
                            .clickable { onNavigate(dest.route) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("nav_${dest.route}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SssStyledIcon(
                            icon = dest.icon,
                            contentDescription = dest.label,
                            isSelected = selected,
                            sizeDp = 34.dp
                        )
                        if (selected) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = dest.label,
                                style = MaterialTheme.typography.labelLarge,
                                color = palette.primaryNeon,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    } else {
        // CLASSIC_DOCK
        NavigationBar(
            containerColor = palette.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = palette.primaryNeon.copy(alpha = 0.3f * glowAlpha),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                )
        ) {
            destinations.forEach { dest ->
                val selected = currentRoute == dest.route
                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigate(dest.route) },
                    modifier = Modifier.testTag("nav_${dest.route}"),
                    icon = {
                        SssStyledIcon(
                            icon = dest.icon,
                            contentDescription = dest.label,
                            isSelected = selected,
                            sizeDp = 34.dp
                        )
                    },
                    label = {
                        Text(
                            text = dest.label,
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = palette.primaryNeon,
                        selectedTextColor = palette.primaryNeon,
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = palette.onSurfaceMuted,
                        unselectedTextColor = palette.onSurfaceMuted
                    )
                )
            }
        }
    }
}

@Composable
fun SssCompactNavigationRail(
    destinations: List<SssNavDestination>,
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val config = LocalSssVisualConfig.current
    val palette = config.neonTheme

    NavigationRail(
        containerColor = palette.surface,
        modifier = Modifier
            .fillMaxHeight()
            .border(1.dp, palette.primaryNeon.copy(alpha = 0.28f))
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "SSS",
            fontFamily = OrbitronFontFamily,
            style = MaterialTheme.typography.titleMedium,
            color = palette.primaryNeon,
            modifier = Modifier.padding(vertical = 12.dp)
        )
        destinations.forEach { dest ->
            val selected = currentRoute == dest.route
            NavigationRailItem(
                selected = selected,
                onClick = { onNavigate(dest.route) },
                modifier = Modifier.testTag("rail_nav_${dest.route}"),
                icon = {
                    SssStyledIcon(
                        icon = dest.icon,
                        contentDescription = dest.label,
                        isSelected = selected,
                        sizeDp = 36.dp
                    )
                },
                label = {
                    Text(
                        text = dest.label,
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = palette.primaryNeon,
                    selectedTextColor = palette.primaryNeon,
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = palette.onSurfaceMuted,
                    unselectedTextColor = palette.onSurfaceMuted
                )
            )
        }
    }
}
