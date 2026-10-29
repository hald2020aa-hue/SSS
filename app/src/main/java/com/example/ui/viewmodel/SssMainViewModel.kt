package com.example.ui.viewmodel

import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.MangaBookmark
import com.example.data.model.ReadingSessionLog
import com.example.data.model.UiCustomizationProfile
import com.example.data.remote.KotatsuMangaItem
import com.example.data.remote.MangaCatalogueRepository
import com.example.data.remote.MangaChapterItem
import com.example.data.repository.SssCloudRepository
import com.example.ui.theme.SssNeonTheme
import com.example.ui.theme.SssVisualConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class UxOptimizationRecommendation(
    val id: String,
    val title: String,
    val description: String,
    val impactBadge: String,
    val targetThemeId: String? = null,
    val targetLayoutMode: String? = null,
    val targetIconStyle: String? = null,
    val targetReaderMode: String? = null,
    val targetGlowIntensity: Int? = null
)

class SssMainViewModel(
    private val cloudRepository: SssCloudRepository,
    private val catalogueRepository: MangaCatalogueRepository,
    private val currentUserId: String,
    private val isCloudAuthenticated: Boolean = true,
    initialNeonTheme: SssNeonTheme = SssNeonTheme.CYBER_CYAN
) : ViewModel() {

    private val _visualConfig = MutableStateFlow(
        SssVisualConfig(neonTheme = initialNeonTheme)
    )
    val visualConfig: StateFlow<SssVisualConfig> = _visualConfig.asStateFlow()

    private val _activeProfileId = MutableStateFlow<String?>(null)
    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    // Local state flows used when user selects "Skip and Sign In Later" (strictly auth-gated: zero Firestore calls when unauthenticated)
    private val _localProfiles = MutableStateFlow(
        listOf(
            UiCustomizationProfile(
                id = "local_preset_1",
                userId = currentUserId,
                profileName = "SSS Cyber Samurai (محلي)",
                neonThemeId = initialNeonTheme.id,
                layoutMode = "GRID_3",
                iconStyle = "NEON_GLOW",
                navigationBarStyle = "FLOATING_PILL",
                homeSectionOrder = "FEATURED_RECENT_POPULAR",
                cardCornerRadiusDp = 16L,
                neonGlowIntensity = 85L,
                readerMode = "WEBTOON_VERTICAL",
                deviceName = "${Build.MANUFACTURER} ${Build.MODEL}".take(40).ifBlank { "Android Device" },
                isActive = true
            )
        )
    )

    private val _localLibrary = MutableStateFlow(
        listOf(
            MangaBookmark(
                id = "sss_cyber_ronin",
                userId = currentUserId,
                mangaKey = "sss_cyber_ronin",
                title = "SSS-Class Cyber Ronin",
                coverUrl = "",
                sourceName = "Kotatsu Arabic",
                author = "Kenji Takahashi",
                genre = "Cyberpunk",
                category = "READING",
                lastReadChapter = 18L,
                totalChapters = 142L,
                rating = 4.9
            ),
            MangaBookmark(
                id = "sss_shadow_sovereign",
                userId = currentUserId,
                mangaKey = "sss_shadow_sovereign",
                title = "Solo Sovereign: Neon Abyss",
                coverUrl = "",
                sourceName = "Asura Neon",
                author = "Chugong & Redice",
                genre = "Fantasy",
                category = "FAVORITES",
                lastReadChapter = 64L,
                totalChapters = 195L,
                rating = 4.95
            )
        )
    )

    private val _localSessions = MutableStateFlow(
        listOf(
            ReadingSessionLog(
                id = "session_1",
                userId = currentUserId,
                mangaTitle = "SSS-Class Cyber Ronin",
                genre = "Cyberpunk",
                minutesSpent = 35L,
                pagesRead = 92L,
                chaptersCompleted = 3L,
                neonThemeUsed = "CYBER_CYAN",
                layoutModeUsed = "GRID_3"
            ),
            ReadingSessionLog(
                id = "session_2",
                userId = currentUserId,
                mangaTitle = "Solo Sovereign: Neon Abyss",
                genre = "Fantasy",
                minutesSpent = 48L,
                pagesRead = 134L,
                chaptersCompleted = 4L,
                neonThemeUsed = "NEON_MAGENTA",
                layoutModeUsed = "GRID_2"
            )
        )
    )

    // Explore & Search State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedSource = MutableStateFlow("ALL")
    val selectedSource: StateFlow<String> = _selectedSource.asStateFlow()

    private val _selectedGenre = MutableStateFlow("ALL")
    val selectedGenre: StateFlow<String> = _selectedGenre.asStateFlow()

    private val _exploreCatalog = MutableStateFlow<List<KotatsuMangaItem>>(emptyList())
    val exploreCatalog: StateFlow<List<KotatsuMangaItem>> = _exploreCatalog.asStateFlow()

    private val _isCatalogLoading = MutableStateFlow(false)
    val isCatalogLoading: StateFlow<Boolean> = _isCatalogLoading.asStateFlow()

    // Selected Manga Detail & Reader State
    private val _selectedManga = MutableStateFlow<KotatsuMangaItem?>(null)
    val selectedManga: StateFlow<KotatsuMangaItem?> = _selectedManga.asStateFlow()

    private val _chapters = MutableStateFlow<List<MangaChapterItem>>(emptyList())
    val chapters: StateFlow<List<MangaChapterItem>> = _chapters.asStateFlow()

    private val _activeReadingChapter = MutableStateFlow<MangaChapterItem?>(null)
    val activeReadingChapter: StateFlow<MangaChapterItem?> = _activeReadingChapter.asStateFlow()

    // Strict Auth-Gating: Only query Firestore when isCloudAuthenticated == true
    val uiProfilesState: StateFlow<UiState<List<UiCustomizationProfile>>> =
        (if (isCloudAuthenticated) {
            cloudRepository.observeUiProfiles(currentUserId)
        } else {
            _localProfiles
        })
            .map<List<UiCustomizationProfile>, UiState<List<UiCustomizationProfile>>> { list ->
                val active = list.firstOrNull { it.isActive } ?: list.firstOrNull()
                if (active != null && _activeProfileId.value == null) {
                    _activeProfileId.value = active.id
                    applyProfileLocally(active)
                }
                UiState.Success(list)
            }
            .catch { error ->
                Log.w(TAG, "Error observing UI profiles", error)
                emit(UiState.Error(error.message ?: "تعذر تحميل ملفات التخصيص السحابية"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = UiState.Loading
            )

    val libraryState: StateFlow<UiState<List<MangaBookmark>>> =
        (if (isCloudAuthenticated) {
            cloudRepository.observeLibrary(currentUserId)
        } else {
            _localLibrary
        })
            .map<List<MangaBookmark>, UiState<List<MangaBookmark>>> { UiState.Success(it) }
            .catch { error ->
                Log.w(TAG, "Error observing library", error)
                emit(UiState.Error(error.message ?: "تعذر تحميل المكتبة السحابية"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = UiState.Loading
            )

    val readingSessionsState: StateFlow<UiState<List<ReadingSessionLog>>> =
        (if (isCloudAuthenticated) {
            cloudRepository.observeReadingSessions(currentUserId)
        } else {
            _localSessions
        })
            .map<List<ReadingSessionLog>, UiState<List<ReadingSessionLog>>> { UiState.Success(it) }
            .catch { error ->
                Log.w(TAG, "Error observing reading sessions", error)
                emit(UiState.Error(error.message ?: "تعذر تحميل بيانات التحليلات"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = UiState.Loading
            )

    init {
        refreshCatalog()
    }

    fun clearBanner() {
        _statusBanner.value = null
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        refreshCatalog()
    }

    fun updateSourceFilter(source: String) {
        _selectedSource.value = source
        refreshCatalog()
    }

    fun updateGenreFilter(genre: String) {
        _selectedGenre.value = genre
    }

    fun refreshCatalog() {
        viewModelScope.launch {
            _isCatalogLoading.value = true
            val list = catalogueRepository.loadExploreCatalog(
                searchQuery = _searchQuery.value,
                sourceFilter = _selectedSource.value
            )
            _exploreCatalog.value = list
            _isCatalogLoading.value = false
        }
    }

    fun openMangaDetail(manga: KotatsuMangaItem) {
        _selectedManga.value = manga
        viewModelScope.launch {
            _chapters.value = catalogueRepository.loadChaptersForManga(manga)
        }
    }

    fun closeMangaDetail() {
        _activeReadingChapter.value = null
        _selectedManga.value = null
    }

    fun openChapterReader(chapter: MangaChapterItem) {
        _activeReadingChapter.value = chapter
    }

    fun closeChapterReader() {
        _activeReadingChapter.value = null
    }

    // --- UI CUSTOMIZATION & CLOUD SYNC ---

    fun selectNeonTheme(theme: SssNeonTheme) {
        _visualConfig.value = _visualConfig.value.copy(neonTheme = theme)
        syncActiveConfigToCloud("ثيم ${theme.arabicName}")
    }

    fun updateLayoutMode(layoutMode: String) {
        _visualConfig.value = _visualConfig.value.copy(layoutMode = layoutMode)
        syncActiveConfigToCloud("توزيع العناصر ($layoutMode)")
    }

    fun updateIconStyle(iconStyle: String) {
        _visualConfig.value = _visualConfig.value.copy(iconStyle = iconStyle)
        syncActiveConfigToCloud("تنسيق الأيقونات ($iconStyle)")
    }

    fun updateNavigationBarStyle(navStyle: String) {
        _visualConfig.value = _visualConfig.value.copy(navigationBarStyle = navStyle)
        syncActiveConfigToCloud("شريط التنقل ($navStyle)")
    }

    fun updateHomeSectionOrder(order: String) {
        _visualConfig.value = _visualConfig.value.copy(homeSectionOrder = order)
        syncActiveConfigToCloud("ترتيب أقسام الرئيسية")
    }

    fun updateCardCornerRadius(radiusDp: Int) {
        _visualConfig.value = _visualConfig.value.copy(cardCornerRadiusDp = radiusDp.coerceIn(4, 32))
    }

    fun updateNeonGlowIntensity(intensity: Int) {
        _visualConfig.value = _visualConfig.value.copy(neonGlowIntensity = intensity.coerceIn(0, 100))
    }

    fun updateReaderMode(mode: String) {
        _visualConfig.value = _visualConfig.value.copy(readerMode = mode)
        syncActiveConfigToCloud("وضع القارئ ($mode)")
    }

    fun saveCustomPresetToCloud(presetName: String) {
        val cfg = _visualConfig.value
        val device = "${Build.MANUFACTURER} ${Build.MODEL}".take(60).ifBlank { "SSS Android" }
        val profile = UiCustomizationProfile(
            id = "preset_${System.currentTimeMillis()}",
            userId = currentUserId,
            profileName = presetName.ifBlank { "نمط SSS مخصص" },
            neonThemeId = cfg.neonTheme.id,
            layoutMode = cfg.layoutMode,
            iconStyle = cfg.iconStyle,
            navigationBarStyle = cfg.navigationBarStyle,
            homeSectionOrder = cfg.homeSectionOrder,
            cardCornerRadiusDp = cfg.cardCornerRadiusDp.toLong(),
            neonGlowIntensity = cfg.neonGlowIntensity.toLong(),
            readerMode = cfg.readerMode,
            deviceName = device,
            isActive = true
        )
        if (!isCloudAuthenticated) {
            _localProfiles.value = _localProfiles.value + profile
            _statusBanner.value = "تم حفظ النمط «${profile.profileName}» محلياً (سجل الدخول لمزامنته سحابياً)"
            return
        }
        viewModelScope.launch {
            val result = cloudRepository.saveUiProfile(profile)
            result.onSuccess { id ->
                _activeProfileId.value = id
                _statusBanner.value = "تم حفظ ومزامنة النمط «${profile.profileName}» سحابياً"
            }.onFailure { err ->
                _statusBanner.value = "تعذر الحفظ السحابي: ${err.localizedMessage}"
            }
        }
    }

    fun syncActiveConfigToCloud(changeLabel: String = "الإعدادات الحالية") {
        val cfg = _visualConfig.value
        val device = "${Build.MANUFACTURER} ${Build.MODEL}".take(60).ifBlank { "SSS Android" }
        val existingId = _activeProfileId.value ?: "default_sss_profile"
        val profile = UiCustomizationProfile(
            id = existingId,
            userId = currentUserId,
            profileName = "ملف المزامنة النشط ($device)",
            neonThemeId = cfg.neonTheme.id,
            layoutMode = cfg.layoutMode,
            iconStyle = cfg.iconStyle,
            navigationBarStyle = cfg.navigationBarStyle,
            homeSectionOrder = cfg.homeSectionOrder,
            cardCornerRadiusDp = cfg.cardCornerRadiusDp.toLong(),
            neonGlowIntensity = cfg.neonGlowIntensity.toLong(),
            readerMode = cfg.readerMode,
            deviceName = device,
            isActive = true
        )
        if (!isCloudAuthenticated) {
            _localProfiles.value = listOf(profile) + _localProfiles.value.filterNot { it.id == existingId }
            _statusBanner.value = "تم تطبيق $changeLabel (الوضع المحلي)"
            return
        }
        viewModelScope.launch {
            val res = cloudRepository.saveUiProfile(profile, docId = existingId)
            res.onSuccess {
                _activeProfileId.value = existingId
                _statusBanner.value = "تمت مزامنة $changeLabel مع السحابة"
            }
        }
    }

    fun applyCloudProfile(profile: UiCustomizationProfile) {
        _activeProfileId.value = profile.id
        applyProfileLocally(profile)
        _statusBanner.value = "تم تطبيق نمط «${profile.profileName}» من ${profile.deviceName}"
    }

    fun deleteCloudProfile(profileId: String) {
        if (!isCloudAuthenticated) {
            _localProfiles.value = _localProfiles.value.filterNot { it.id == profileId }
            _statusBanner.value = "تم حذف النمط المخصص"
            return
        }
        viewModelScope.launch {
            cloudRepository.deleteUiProfile(profileId).onSuccess {
                _statusBanner.value = "تم حذف النمط المخصص من السحابة"
            }
        }
    }

    private fun applyProfileLocally(profile: UiCustomizationProfile) {
        _visualConfig.value = SssVisualConfig(
            neonTheme = SssNeonTheme.fromId(profile.neonThemeId),
            layoutMode = profile.layoutMode,
            iconStyle = profile.iconStyle,
            navigationBarStyle = profile.navigationBarStyle,
            homeSectionOrder = profile.homeSectionOrder,
            cardCornerRadiusDp = profile.cardCornerRadiusDp.toInt().coerceIn(4, 32),
            neonGlowIntensity = profile.neonGlowIntensity.toInt().coerceIn(0, 100),
            readerMode = profile.readerMode
        )
    }

    // --- LIBRARY & BOOKMARK ACTIONS ---

    fun bookmarkManga(manga: KotatsuMangaItem, category: String = "READING", lastChapter: Int = 1) {
        val bookmark = MangaBookmark(
            id = manga.id,
            userId = currentUserId,
            mangaKey = manga.id,
            title = manga.title,
            coverUrl = manga.coverUrl,
            sourceName = manga.sourceName,
            author = manga.author,
            genre = manga.genre,
            category = category,
            lastReadChapter = lastChapter.toLong(),
            totalChapters = manga.totalChapters.toLong(),
            rating = manga.rating
        )
        if (!isCloudAuthenticated) {
            _localLibrary.value = listOf(bookmark) + _localLibrary.value.filterNot { it.id == manga.id }
            _statusBanner.value = "تمت إضافة «${manga.title}» إلى مكتبتك"
            return
        }
        viewModelScope.launch {
            cloudRepository.saveMangaBookmark(bookmark, customId = manga.id).onSuccess {
                _statusBanner.value = "تمت إضافة «${manga.title}» إلى مكتبتك السحابية"
            }
        }
    }

    fun updateBookmarkProgress(bookmark: MangaBookmark, newChapter: Long, newCategory: String = bookmark.category) {
        val updated = bookmark.copy(
            lastReadChapter = newChapter.coerceIn(0L, bookmark.totalChapters.coerceAtLeast(1L)),
            category = newCategory
        )
        if (!isCloudAuthenticated) {
            _localLibrary.value = _localLibrary.value.map { if (it.id == bookmark.id) updated else it }
            _statusBanner.value = "تم تحديث تقدم «${bookmark.title}» إلى الفصل $newChapter"
            return
        }
        viewModelScope.launch {
            cloudRepository.saveMangaBookmark(updated, customId = bookmark.id).onSuccess {
                _statusBanner.value = "تم تحديث تقدم «${bookmark.title}» إلى الفصل $newChapter"
            }
        }
    }

    fun removeBookmark(mangaDocId: String) {
        if (!isCloudAuthenticated) {
            _localLibrary.value = _localLibrary.value.filterNot { it.id == mangaDocId }
            _statusBanner.value = "تمت إزالة العمل من المكتبة"
            return
        }
        viewModelScope.launch {
            cloudRepository.removeMangaBookmark(mangaDocId).onSuccess {
                _statusBanner.value = "تمت إزالة العمل من المكتبة"
            }
        }
    }

    // --- USAGE ANALYTICS & CONTINUOUS UX OPTIMIZATION ---

    fun completeChapterAndLogSession(manga: KotatsuMangaItem, chapter: MangaChapterItem, minutesSpent: Int = 18) {
        bookmarkManga(manga, category = "READING", lastChapter = chapter.number)
        val cfg = _visualConfig.value
        val session = ReadingSessionLog(
            id = "session_${System.currentTimeMillis()}",
            userId = currentUserId,
            mangaTitle = manga.title,
            genre = manga.genre,
            minutesSpent = minutesSpent.toLong(),
            pagesRead = chapter.pageCount.toLong(),
            chaptersCompleted = 1L,
            neonThemeUsed = cfg.neonTheme.id,
            layoutModeUsed = cfg.layoutMode
        )
        if (!isCloudAuthenticated) {
            _localSessions.value = _localSessions.value + session
            _statusBanner.value = "تم تسجيل جلسة قراءة الفصل ${chapter.number} وتحديث التحليلات"
            return
        }
        viewModelScope.launch {
            cloudRepository.logReadingSession(session).onSuccess {
                _statusBanner.value = "تم تسجيل جلسة قراءة الفصل ${chapter.number} وتحديث التحليلات"
            }
        }
    }

    fun logCustomReadingSession(
        mangaTitle: String,
        genre: String,
        minutesSpent: Int,
        pagesRead: Int,
        chaptersCompleted: Int
    ) {
        val cfg = _visualConfig.value
        val session = ReadingSessionLog(
            id = "session_${System.currentTimeMillis()}",
            userId = currentUserId,
            mangaTitle = mangaTitle,
            genre = genre,
            minutesSpent = minutesSpent.toLong(),
            pagesRead = pagesRead.toLong(),
            chaptersCompleted = chaptersCompleted.toLong(),
            neonThemeUsed = cfg.neonTheme.id,
            layoutModeUsed = cfg.layoutMode
        )
        if (!isCloudAuthenticated) {
            _localSessions.value = _localSessions.value + session
            _statusBanner.value = "تم تسجيل بيانات الاستخدام بنجاح"
            return
        }
        viewModelScope.launch {
            cloudRepository.logReadingSession(session).onSuccess {
                _statusBanner.value = "تم تسجيل بيانات الاستخدام بنجاح في السحابة"
            }
        }
    }

    fun applyUxRecommendation(rec: UxOptimizationRecommendation) {
        var updated = _visualConfig.value
        rec.targetThemeId?.let { updated = updated.copy(neonTheme = SssNeonTheme.fromId(it)) }
        rec.targetLayoutMode?.let { updated = updated.copy(layoutMode = it) }
        rec.targetIconStyle?.let { updated = updated.copy(iconStyle = it) }
        rec.targetReaderMode?.let { updated = updated.copy(readerMode = it) }
        rec.targetGlowIntensity?.let { updated = updated.copy(neonGlowIntensity = it) }
        _visualConfig.value = updated
        syncActiveConfigToCloud(rec.title)
    }

    private companion object {
        const val TAG = "SssMainVM"
    }
}
