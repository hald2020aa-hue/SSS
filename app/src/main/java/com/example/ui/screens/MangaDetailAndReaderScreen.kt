package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.remote.KotatsuMangaItem
import com.example.data.remote.MangaChapterItem
import com.example.ui.components.SssMangaCoverArt
import com.example.ui.components.SssStyledIcon
import com.example.ui.theme.LocalSssVisualConfig
import com.example.ui.theme.OrbitronFontFamily

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MangaDetailScreen(
    manga: KotatsuMangaItem,
    chapters: List<MangaChapterItem>,
    onBack: () -> Unit,
    onBookmark: (KotatsuMangaItem) -> Unit,
    onReadChapter: (MangaChapterItem) -> Unit
) {
    BackHandler { onBack() }
    val config = LocalSssVisualConfig.current
    val palette = config.neonTheme
    val cardRadius = config.cardCornerRadiusDp.dp

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .testTag("manga_detail_screen"),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(manga.accentHex).copy(alpha = 0.24f),
                                palette.background
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("detail_back_button")
                    ) {
                        SssStyledIcon(
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            isSelected = true
                        )
                    }
                    Text(
                        text = manga.sourceName.uppercase(),
                        fontFamily = OrbitronFontFamily,
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.primaryNeon
                    )
                    IconButton(
                        onClick = { onBookmark(manga) },
                        modifier = Modifier.testTag("detail_bookmark_button")
                    ) {
                        SssStyledIcon(
                            icon = Icons.Default.BookmarkAdd,
                            contentDescription = "حفظ في المكتبة",
                            isSelected = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    SssMangaCoverArt(
                        title = manga.title,
                        genre = manga.genre,
                        coverUrl = manga.coverUrl,
                        accentHex = manga.accentHex,
                        modifier = Modifier
                            .size(width = 130.dp, height = 184.dp)
                            .clip(RoundedCornerShape(cardRadius))
                            .border(1.5.dp, palette.primaryNeon, RoundedCornerShape(cardRadius))
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = manga.title,
                            style = MaterialTheme.typography.headlineMedium,
                            color = palette.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = manga.arabicSubtitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.secondaryNeon
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "المؤلف: ${manga.author} • ${manga.status}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.onSurfaceMuted
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFB703),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${manga.rating} • ${chapters.size} فصل متاح",
                                style = MaterialTheme.typography.labelLarge,
                                color = palette.onBackground
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                chapters.firstOrNull()?.let { onReadChapter(it) }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.primaryNeon,
                                contentColor = Color(0xFF04060E)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("start_reading_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ابدأ القراءة الآن", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    manga.tags.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(palette.surfaceElevated)
                                .border(1.dp, palette.primaryNeon.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(tag, style = MaterialTheme.typography.labelLarge, color = palette.primaryNeon)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = manga.synopsis,
                    style = MaterialTheme.typography.bodyLarge,
                    color = palette.onBackground
                )
            }
        }

        item {
            Text(
                text = "قائمة الفصول المتوفرة (${chapters.size})",
                style = MaterialTheme.typography.titleLarge,
                color = palette.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        items(chapters, key = { it.id }) { chapter ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 5.dp)
                    .border(1.dp, palette.primaryNeon.copy(alpha = 0.28f), RoundedCornerShape(cardRadius))
                    .clickable { onReadChapter(chapter) }
                    .testTag("chapter_row_${chapter.number}"),
                shape = RoundedCornerShape(cardRadius),
                colors = CardDefaults.cardColors(containerColor = palette.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = chapter.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${chapter.scanlator} • ${chapter.pageCount} لوحة عالية الدقة",
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.onSurfaceMuted
                        )
                    }
                    SssStyledIcon(
                        icon = Icons.Default.MenuBook,
                        contentDescription = "قراءة الفصل",
                        isSelected = true,
                        sizeDp = 36.dp
                    )
                }
            }
        }
    }
}

@Composable
fun SssChapterReaderScreen(
    manga: KotatsuMangaItem,
    chapter: MangaChapterItem,
    onBackToDetail: () -> Unit,
    onFinishChapterAndLog: () -> Unit
) {
    BackHandler { onBackToDetail() }
    val config = LocalSssVisualConfig.current
    val palette = config.neonTheme
    var currentPage by remember { mutableIntStateOf(1) }
    val totalPages = chapter.pageCount.coerceAtLeast(8)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF04060B))
            .testTag("chapter_reader_screen")
    ) {
        // Top Reader HUD Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(palette.surface.copy(alpha = 0.95f))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackToDetail,
                modifier = Modifier.testTag("reader_close_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "إغلاق القارئ",
                    tint = palette.primaryNeon
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Text(
                    text = manga.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = palette.onBackground,
                    maxLines = 1
                )
                Text(
                    text = "${chapter.title} • وضع ${config.readerMode}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.secondaryNeon,
                    maxLines = 1
                )
            }
            Button(
                onClick = {
                    onFinishChapterAndLog()
                    onBackToDetail()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.primaryNeon,
                    contentColor = Color(0xFF05070E)
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier
                    .height(36.dp)
                    .testTag("complete_chapter_button")
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("إنهاء الفصل", style = MaterialTheme.typography.labelLarge)
            }
        }

        if (config.readerMode == "WEBTOON_VERTICAL") {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items((1..totalPages).toList()) { pageNum ->
                    CyberMangaPagePanel(
                        mangaTitle = manga.title,
                        chapterNum = chapter.number,
                        pageNum = pageNum,
                        totalPages = totalPages,
                        accentHex = manga.accentHex
                    )
                }
            }
        } else {
            // Paged Reader Mode (RTL / LTR)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CyberMangaPagePanel(
                    mangaTitle = manga.title,
                    chapterNum = chapter.number,
                    pageNum = currentPage,
                    totalPages = totalPages,
                    accentHex = manga.accentHex
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(palette.surface)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { if (currentPage > 1) currentPage-- },
                    enabled = currentPage > 1
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.NavigateBefore,
                        contentDescription = "السابق",
                        tint = palette.primaryNeon
                    )
                }
                Text(
                    text = "صفحة $currentPage من $totalPages",
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.onBackground
                )
                IconButton(
                    onClick = { if (currentPage < totalPages) currentPage++ },
                    enabled = currentPage < totalPages
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.NavigateNext,
                        contentDescription = "التالي",
                        tint = palette.primaryNeon
                    )
                }
            }
        }
    }
}

@Composable
private fun CyberMangaPagePanel(
    mangaTitle: String,
    chapterNum: Int,
    pageNum: Int,
    totalPages: Int,
    accentHex: Long
) {
    val palette = LocalSssVisualConfig.current.neonTheme
    val accent = Color(accentHex)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp)
            .border(1.5.dp, palette.primaryNeon.copy(alpha = 0.45f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF090C18))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Dynamic manga speed lines & dramatic diagonal panel splits
                for (i in 0..8) {
                    val ratio = i / 8f
                    drawLine(
                        color = accent.copy(alpha = 0.16f),
                        start = Offset(w * ratio, 0f),
                        end = Offset(w * (1f - ratio), h),
                        strokeWidth = 2f
                    )
                }

                // Panel divider frame
                drawLine(
                    color = palette.primaryNeon,
                    start = Offset(0f, h * 0.48f),
                    end = Offset(w, h * 0.38f),
                    strokeWidth = 4f
                )

                // Glowing energy orb inside top panel
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(palette.primaryNeon.copy(alpha = 0.45f), Color.Transparent),
                        center = Offset(w * 0.7f, h * 0.22f),
                        radius = w * 0.28f
                    ),
                    radius = w * 0.28f,
                    center = Offset(w * 0.7f, h * 0.22f)
                )

                // Stylized cyber blade / aura in bottom panel
                val aura = Path().apply {
                    moveTo(w * 0.15f, h * 0.85f)
                    lineTo(w * 0.55f, h * 0.52f)
                    lineTo(w * 0.85f, h * 0.78f)
                    close()
                }
                drawPath(aura, color = palette.secondaryNeon.copy(alpha = 0.2f))
                drawPath(aura, color = palette.secondaryNeon, style = Stroke(width = 3f))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xCC05070E))
                            .border(1.dp, palette.primaryNeon, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "PANEL #$pageNum / $totalPages",
                            fontFamily = OrbitronFontFamily,
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.primaryNeon
                        )
                    }
                    Text(
                        text = "CH.$chapterNum",
                        fontFamily = OrbitronFontFamily,
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.secondaryNeon
                    )
                }

                // Manga dialogue bubble
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xE6101528))
                        .border(1.dp, palette.primaryNeon.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "«في المستوى SSS، تتجاوز سرعة النيون حدود الزمن نفسه... الفصل $chapterNum - اللوحة $pageNum من $mangaTitle»",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                }
            }
        }
    }
}
