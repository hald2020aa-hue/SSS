package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.remote.KotatsuMangaItem
import com.example.ui.components.SssMangaCoverArt
import com.example.ui.components.SssStyledIcon
import com.example.ui.theme.LocalSssVisualConfig
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.SssNeonTheme

@Composable
fun ExploreScreen(
    catalog: List<KotatsuMangaItem>,
    isLoading: Boolean,
    searchQuery: String,
    selectedSource: String,
    selectedGenre: String,
    onSearchChange: (String) -> Unit,
    onSourceChange: (String) -> Unit,
    onGenreChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onQuickThemeSwitch: (SssNeonTheme) -> Unit,
    onCycleLayoutMode: () -> Unit,
    onOpenManga: (KotatsuMangaItem) -> Unit,
    onQuickBookmark: (KotatsuMangaItem) -> Unit,
    onOpenStudio: () -> Unit
) {
    val config = LocalSssVisualConfig.current
    val palette = config.neonTheme
    val glowAlpha = (config.neonGlowIntensity / 100f).coerceIn(0.15f, 1f)
    val cardRadius = config.cardCornerRadiusDp.dp

    val filteredCatalog = catalog.filter {
        selectedGenre == "ALL" || it.genre.equals(selectedGenre, ignoreCase = true) ||
            it.tags.any { tag -> tag.equals(selectedGenre, ignoreCase = true) }
    }

    val featuredItems = filteredCatalog.filter { it.isFeatured }.ifEmpty { filteredCatalog.take(3) }
    val popularItems = filteredCatalog.sortedByDescending { it.rating }
    val recentItems = filteredCatalog.sortedByDescending { it.totalChapters }

    val sectionOrderList = when (config.homeSectionOrder) {
        "RECENT_FEATURED_POPULAR" -> listOf("RECENT", "FEATURED", "POPULAR")
        "POPULAR_FEATURED_RECENT" -> listOf("POPULAR", "FEATURED", "RECENT")
        else -> listOf("FEATURED", "RECENT", "POPULAR")
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .testTag("explore_screen_list"),
        contentPadding = PaddingValues(bottom = 110.dp)
    ) {
        // Top SSS Neon Header & Live 6-Neon Strip
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                palette.primaryNeon.copy(alpha = 0.14f * glowAlpha),
                                palette.background
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(palette.surfaceElevated)
                                .border(1.8.dp, palette.primaryNeon, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "SSS",
                                fontFamily = OrbitronFontFamily,
                                style = MaterialTheme.typography.titleMedium,
                                color = palette.primaryNeon,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "KOTATSU • SSS PRO",
                                    fontFamily = OrbitronFontFamily,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = palette.secondaryNeon
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = "Cloud Synced",
                                    tint = palette.primaryNeon,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = "بوابة المانجا النيونية",
                                style = MaterialTheme.typography.headlineMedium,
                                color = palette.onBackground
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .clickable { onCycleLayoutMode() }
                                .testTag("cycle_layout_button")
                        ) {
                            SssStyledIcon(
                                icon = Icons.Default.GridView,
                                contentDescription = "تغيير التوزيع",
                                isSelected = true,
                                sizeDp = 42.dp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clickable { onOpenStudio() }
                                .testTag("open_studio_shortcut")
                        ) {
                            SssStyledIcon(
                                icon = Icons.Default.Tune,
                                contentDescription = "استوديو التخصيص",
                                isSelected = false,
                                sizeDp = 42.dp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick 6-Neon Theme Switcher Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SssNeonTheme.entries.forEach { neon ->
                        val active = neon == palette
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (active) neon.primaryNeon.copy(alpha = 0.2f) else palette.surface
                                )
                                .border(
                                    width = if (active) 1.6.dp else 1.dp,
                                    color = if (active) neon.primaryNeon else neon.primaryNeon.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(50)
                                )
                                .clickable { onQuickThemeSwitch(neon) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("quick_neon_${neon.id}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(neon.primaryNeon)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = neon.arabicName.substringBefore(" ("),
                                style = MaterialTheme.typography.labelLarge,
                                color = if (active) neon.primaryNeon else palette.onBackground
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search Bar + Refresh
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("manga_search_input"),
                    placeholder = {
                        Text(
                            text = "ابحث في MangaDex ومصادر Kotatsu...",
                            color = palette.onSurfaceMuted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = palette.primaryNeon
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = onRefresh) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = palette.primaryNeon,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "تحديث",
                                    tint = palette.secondaryNeon
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(cardRadius),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = palette.primaryNeon,
                        unfocusedBorderColor = palette.primaryNeon.copy(alpha = 0.35f),
                        focusedContainerColor = palette.surface,
                        unfocusedContainerColor = palette.surface,
                        focusedTextColor = palette.onBackground,
                        unfocusedTextColor = palette.onBackground
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Kotatsu Multi-Source Filter Pills
                val sources = listOf("ALL", "MangaDex", "Kotatsu Arabic", "Asura Neon", "Flame Scans")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sources.forEach { src ->
                        val selected = selectedSource == src
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (selected) palette.primaryNeon else palette.surfaceElevated
                                )
                                .clickable { onSourceChange(src) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("source_filter_$src")
                        ) {
                            Text(
                                text = if (src == "ALL") "كل المصادر (All)" else src,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) Color(0xFF04060E) else palette.onBackground
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Genre Filter Pills
                val genres = listOf("ALL", "Cyberpunk", "Action", "Fantasy", "Sci-Fi", "Mecha", "Supernatural")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = palette.secondaryNeon,
                        modifier = Modifier.size(18.dp)
                    )
                    genres.forEach { g ->
                        val selected = selectedGenre == g
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    1.dp,
                                    if (selected) palette.secondaryNeon else palette.onSurfaceMuted.copy(alpha = 0.25f),
                                    RoundedCornerShape(8.dp)
                                )
                                .background(
                                    if (selected) palette.secondaryNeon.copy(alpha = 0.18f) else Color.Transparent
                                )
                                .clickable { onGenreChange(g) }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (g == "ALL") "كل التصنيفات" else g,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (selected) palette.secondaryNeon else palette.onSurfaceMuted
                            )
                        }
                    }
                }
            }
        }

        // Dynamic Section Ordering controlled by user's `homeSectionOrder` setting
        sectionOrderList.forEach { sectionType ->
            when (sectionType) {
                "FEATURED" -> {
                    item {
                        SectionHeaderRow(
                            title = "أعمال مختارة بتصنيف SSS",
                            badge = "FEATURED NEON",
                            palette = palette
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(featuredItems, key = { "feat_${it.id}" }) { manga ->
                                FeaturedNeonBannerCard(
                                    manga = manga,
                                    cardRadius = cardRadius,
                                    glowAlpha = glowAlpha,
                                    onClick = { onOpenManga(manga) },
                                    onBookmark = { onQuickBookmark(manga) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                    }
                }

                "RECENT" -> {
                    item {
                        SectionHeaderRow(
                            title = "أحدث الإصدارات المتزامنة",
                            badge = "LIVE UPDATES",
                            palette = palette
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(recentItems.take(8), key = { "rec_${it.id}" }) { manga ->
                                CompactHorizontalMangaCard(
                                    manga = manga,
                                    cardRadius = cardRadius,
                                    onClick = { onOpenManga(manga) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                    }
                }

                "POPULAR" -> {
                    item {
                        SectionHeaderRow(
                            title = "توزيع البطاقات (${config.layoutMode})",
                            badge = "${filteredCatalog.size} MANGA",
                            palette = palette
                        )
                    }

                    // Render catalog according to user-selected layout distribution mode
                    when (config.layoutMode) {
                        "COMPACT_LIST" -> {
                            items(popularItems, key = { "list_${it.id}" }) { manga ->
                                MangaDetailedListRow(
                                    manga = manga,
                                    cardRadius = cardRadius,
                                    glowAlpha = glowAlpha,
                                    onClick = { onOpenManga(manga) },
                                    onBookmark = { onQuickBookmark(manga) }
                                )
                            }
                        }

                        "GRID_2", "STAGGERED_CARDS" -> {
                            val rows = popularItems.chunked(2)
                            items(rows) { rowItems ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    rowItems.forEachIndexed { idx, manga ->
                                        val extraHeight = if (config.layoutMode == "STAGGERED_CARDS" && idx % 2 == 1) 24.dp else 0.dp
                                        MangaGridCard(
                                            manga = manga,
                                            modifier = Modifier.weight(1f),
                                            coverHeight = 200.dp + extraHeight,
                                            cardRadius = cardRadius,
                                            glowAlpha = glowAlpha,
                                            onClick = { onOpenManga(manga) },
                                            onBookmark = { onQuickBookmark(manga) }
                                        )
                                    }
                                    if (rowItems.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }

                        else -> {
                            // Default: GRID_3 (Kotatsu 3-Column Dense Grid)
                            val rows = popularItems.chunked(3)
                            items(rows) { rowItems ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    rowItems.forEach { manga ->
                                        MangaGridCard(
                                            manga = manga,
                                            modifier = Modifier.weight(1f),
                                            coverHeight = 152.dp,
                                            cardRadius = cardRadius,
                                            glowAlpha = glowAlpha,
                                            onClick = { onOpenManga(manga) },
                                            onBookmark = { onQuickBookmark(manga) }
                                        )
                                    }
                                    repeat(3 - rowItems.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeaderRow(
    title: String,
    badge: String,
    palette: SssNeonTheme
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = palette.primaryNeon,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = palette.onBackground
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(palette.primaryNeon.copy(alpha = 0.14f))
                .border(1.dp, palette.primaryNeon.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = badge,
                fontFamily = OrbitronFontFamily,
                style = MaterialTheme.typography.labelSmall,
                color = palette.primaryNeon
            )
        }
    }
}

@Composable
private fun FeaturedNeonBannerCard(
    manga: KotatsuMangaItem,
    cardRadius: androidx.compose.ui.unit.Dp,
    glowAlpha: Float,
    onClick: () -> Unit,
    onBookmark: () -> Unit
) {
    val palette = LocalSssVisualConfig.current.neonTheme
    Card(
        modifier = Modifier
            .width(300.dp)
            .height(186.dp)
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    listOf(
                        palette.primaryNeon.copy(alpha = 0.8f * glowAlpha),
                        palette.secondaryNeon.copy(alpha = 0.5f * glowAlpha)
                    )
                ),
                shape = RoundedCornerShape(cardRadius)
            )
            .clickable { onClick() }
            .testTag("featured_card_${manga.id}"),
        shape = RoundedCornerShape(cardRadius),
        colors = CardDefaults.cardColors(containerColor = palette.surface)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            SssMangaCoverArt(
                title = manga.title,
                genre = manga.genre,
                coverUrl = manga.coverUrl,
                accentHex = manga.accentHex,
                modifier = Modifier
                    .width(118.dp)
                    .fillMaxSize()
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = manga.sourceName.uppercase(),
                        fontFamily = OrbitronFontFamily,
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.secondaryNeon
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = manga.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.onBackground,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = manga.synopsis,
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.onSurfaceMuted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB703),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${manga.rating} • ${manga.totalChapters} فصل",
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.onBackground
                        )
                    }
                    IconButton(
                        onClick = onBookmark,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkAdd,
                            contentDescription = "حفظ في المكتبة",
                            tint = palette.primaryNeon
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactHorizontalMangaCard(
    manga: KotatsuMangaItem,
    cardRadius: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    val palette = LocalSssVisualConfig.current.neonTheme
    Card(
        modifier = Modifier
            .width(136.dp)
            .border(
                1.dp,
                palette.primaryNeon.copy(alpha = 0.3f),
                RoundedCornerShape(cardRadius)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(cardRadius),
        colors = CardDefaults.cardColors(containerColor = palette.surface)
    ) {
        Column {
            SssMangaCoverArt(
                title = manga.title,
                genre = manga.genre,
                coverUrl = manga.coverUrl,
                accentHex = manga.accentHex,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(145.dp)
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = manga.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = palette.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${manga.totalChapters} فصل • ${manga.sourceName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.onSurfaceMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun MangaGridCard(
    manga: KotatsuMangaItem,
    modifier: Modifier = Modifier,
    coverHeight: androidx.compose.ui.unit.Dp,
    cardRadius: androidx.compose.ui.unit.Dp,
    glowAlpha: Float,
    onClick: () -> Unit,
    onBookmark: () -> Unit
) {
    val palette = LocalSssVisualConfig.current.neonTheme
    Card(
        modifier = modifier
            .border(
                1.dp,
                palette.primaryNeon.copy(alpha = 0.38f * glowAlpha),
                RoundedCornerShape(cardRadius)
            )
            .clickable { onClick() }
            .testTag("grid_manga_${manga.id}"),
        shape = RoundedCornerShape(cardRadius),
        colors = CardDefaults.cardColors(containerColor = palette.surface)
    ) {
        Column {
            Box {
                SssMangaCoverArt(
                    title = manga.title,
                    genre = manga.genre,
                    coverUrl = manga.coverUrl,
                    accentHex = manga.accentHex,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(coverHeight)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xCC060812))
                        .border(1.dp, palette.primaryNeon, CircleShape)
                        .clickable { onBookmark() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkAdd,
                        contentDescription = "حفظ",
                        tint = palette.primaryNeon,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = manga.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = palette.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = manga.sourceName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.secondaryNeon,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun MangaDetailedListRow(
    manga: KotatsuMangaItem,
    cardRadius: androidx.compose.ui.unit.Dp,
    glowAlpha: Float,
    onClick: () -> Unit,
    onBookmark: () -> Unit
) {
    val palette = LocalSssVisualConfig.current.neonTheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .border(
                1.dp,
                palette.primaryNeon.copy(alpha = 0.35f * glowAlpha),
                RoundedCornerShape(cardRadius)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(cardRadius),
        colors = CardDefaults.cardColors(containerColor = palette.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SssMangaCoverArt(
                title = manga.title,
                genre = manga.genre,
                coverUrl = manga.coverUrl,
                accentHex = manga.accentHex,
                modifier = Modifier
                    .size(width = 70.dp, height = 94.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = manga.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.onBackground,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = manga.arabicSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.secondaryNeon,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${manga.genre} • ${manga.totalChapters} فصل • ★ ${manga.rating}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.onSurfaceMuted
                )
            }
            IconButton(onClick = onBookmark) {
                SssStyledIcon(
                    icon = Icons.Default.BookmarkAdd,
                    contentDescription = "إضافة للمكتبة",
                    isSelected = true,
                    sizeDp = 38.dp
                )
            }
        }
    }
}
