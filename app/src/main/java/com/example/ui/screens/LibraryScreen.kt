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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.MangaBookmark
import com.example.data.remote.KotatsuMangaItem
import com.example.ui.components.SssMangaCoverArt
import com.example.ui.components.SssStyledIcon
import com.example.ui.theme.LocalSssVisualConfig
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.viewmodel.UiState

@Composable
fun LibraryScreen(
    libraryState: UiState<List<MangaBookmark>>,
    catalog: List<KotatsuMangaItem>,
    onIncrementChapter: (MangaBookmark) -> Unit,
    onChangeCategory: (MangaBookmark, String) -> Unit,
    onRemoveBookmark: (String) -> Unit,
    onOpenFromCatalog: (MangaBookmark) -> Unit,
    onSeedSampleLibrary: () -> Unit
) {
    val config = LocalSssVisualConfig.current
    val palette = config.neonTheme
    val cardRadius = config.cardCornerRadiusDp.dp
    var selectedShelf by remember { mutableStateOf("ALL") }

    val shelves = listOf(
        "ALL" to "الكل",
        "READING" to "أقرأها حالياً",
        "FAVORITES" to "المفضلة SSS",
        "PLAN_TO_READ" to "خطط القراءة",
        "COMPLETED" to "مكتملة"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .padding(horizontal = 16.dp)
            .testTag("library_screen")
    ) {
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CLOUD SYNCED LIBRARY",
                    fontFamily = OrbitronFontFamily,
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.secondaryNeon
                )
                Text(
                    text = "مكتبة Kotatsu السحابية",
                    style = MaterialTheme.typography.headlineMedium,
                    color = palette.onBackground
                )
            }
            SssStyledIcon(
                icon = Icons.Default.CloudSync,
                contentDescription = "متزامن سحابياً",
                isSelected = true,
                sizeDp = 42.dp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Shelf Filter Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            shelves.forEach { (key, label) ->
                val active = selectedShelf == key
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (active) palette.primaryNeon else palette.surfaceElevated)
                        .clickable { selectedShelf = key }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("shelf_tab_$key")
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (active) Color(0xFF05070E) else palette.onBackground
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (libraryState) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("جاري مزامنة المكتبة السحابية...", color = palette.primaryNeon)
                }
            }

            is UiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(libraryState.message, color = Color(0xFFFF4D6D))
                }
            }

            is UiState.Success -> {
                val filtered = libraryState.data.filter {
                    selectedShelf == "ALL" || it.category == selectedShelf
                }

                if (filtered.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, palette.primaryNeon.copy(alpha = 0.35f), RoundedCornerShape(cardRadius)),
                            colors = CardDefaults.cardColors(containerColor = palette.surface),
                            shape = RoundedCornerShape(cardRadius)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                SssStyledIcon(
                                    icon = Icons.Default.Bookmarks,
                                    contentDescription = null,
                                    isSelected = true,
                                    sizeDp = 56.dp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "مكتبتك السحابية فارغة في هذا الرف",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = palette.onBackground
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "احفظ أعمال المانجا من تبويب الاستكشاف أو أضف حزمة أعمال SSS المقترحة بضغطة واحدة لتجربة المزامنة السحابية.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = palette.onSurfaceMuted
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = onSeedSampleLibrary,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = palette.primaryNeon,
                                        contentColor = Color(0xFF04060E)
                                    ),
                                    modifier = Modifier.testTag("seed_library_button")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("إضافة أعمال SSS المختارة للمكتبة", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 110.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filtered, key = { it.id }) { item ->
                            val progress = (item.lastReadChapter.toFloat() / item.totalChapters.coerceAtLeast(1L).toFloat())
                                .coerceIn(0f, 1f)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        palette.primaryNeon.copy(alpha = 0.38f),
                                        RoundedCornerShape(cardRadius)
                                    )
                                    .clickable { onOpenFromCatalog(item) }
                                    .testTag("library_item_${item.id}"),
                                shape = RoundedCornerShape(cardRadius),
                                colors = CardDefaults.cardColors(containerColor = palette.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    SssMangaCoverArt(
                                        title = item.title,
                                        genre = item.genre,
                                        coverUrl = item.coverUrl,
                                        accentHex = 0xFF00F5D4,
                                        modifier = Modifier
                                            .size(width = 76.dp, height = 104.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = item.sourceName,
                                                fontFamily = OrbitronFontFamily,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = palette.secondaryNeon
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(palette.primaryNeon.copy(alpha = 0.16f))
                                                    .clickable {
                                                        val nextCat = when (item.category) {
                                                            "READING" -> "FAVORITES"
                                                            "FAVORITES" -> "COMPLETED"
                                                            "COMPLETED" -> "PLAN_TO_READ"
                                                            else -> "READING"
                                                        }
                                                        onChangeCategory(item, nextCat)
                                                    }
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = item.category,
                                                    fontFamily = OrbitronFontFamily,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = palette.primaryNeon
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = palette.onBackground,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "الفصل ${item.lastReadChapter} من ${item.totalChapters} (${(progress * 100).toInt()}%)",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = palette.onSurfaceMuted
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        LinearProgressIndicator(
                                            progress = { progress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            color = palette.primaryNeon,
                                            trackColor = palette.surfaceElevated
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Button(
                                                onClick = { onIncrementChapter(item) },
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                modifier = Modifier.height(34.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = palette.primaryNeon,
                                                    contentColor = Color(0xFF04060E)
                                                )
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.MenuBook,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("+1 فصل", style = MaterialTheme.typography.labelLarge)
                                            }
                                            IconButton(
                                                onClick = { onRemoveBookmark(item.id) },
                                                modifier = Modifier.size(34.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DeleteOutline,
                                                    contentDescription = "حذف",
                                                    tint = palette.onSurfaceMuted
                                                )
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
    }
}
