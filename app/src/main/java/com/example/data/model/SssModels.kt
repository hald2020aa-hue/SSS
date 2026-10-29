package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UiCustomizationProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val profileName: String = "SSS Cyber Pro",
    val neonThemeId: String = "CYBER_CYAN",
    val layoutMode: String = "GRID_3",
    val iconStyle: String = "NEON_GLOW",
    val navigationBarStyle: String = "FLOATING_PILL",
    val homeSectionOrder: String = "FEATURED_RECENT_POPULAR",
    val cardCornerRadiusDp: Long = 16L,
    val neonGlowIntensity: Long = 85L,
    val readerMode: String = "WEBTOON_VERTICAL",
    val deviceName: String = "Android Studio Device",
    @get:PropertyName("isActive")
    @set:PropertyName("isActive")
    var isActive: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class MangaBookmark(
    @DocumentId val id: String = "",
    val userId: String = "",
    val mangaKey: String = "",
    val title: String = "",
    val coverUrl: String = "",
    val sourceName: String = "MangaDex",
    val author: String = "",
    val genre: String = "Action",
    val category: String = "READING",
    val lastReadChapter: Long = 1L,
    val totalChapters: Long = 100L,
    val rating: Double = 4.8,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ReadingSessionLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val mangaTitle: String = "",
    val genre: String = "Action",
    val minutesSpent: Long = 25L,
    val pagesRead: Long = 60L,
    val chaptersCompleted: Long = 2L,
    val neonThemeUsed: String = "CYBER_CYAN",
    val layoutModeUsed: String = "GRID_3",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
