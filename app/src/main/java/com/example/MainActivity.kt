package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.remote.MangaCatalogueRepository
import com.example.data.repository.SssCloudRepository
import com.example.ui.auth.SssAuthScreen
import com.example.ui.auth.signOut
import com.example.ui.components.SssAdaptiveBottomBar
import com.example.ui.components.SssCompactNavigationRail
import com.example.ui.components.SssNavDestination
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.MangaDetailScreen
import com.example.ui.screens.SssChapterReaderScreen
import com.example.ui.screens.StudioCustomizationScreen
import com.example.ui.theme.SssNeonTheme
import com.example.ui.theme.SssTheme
import com.example.ui.viewmodel.SssMainViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SssRootApp()
        }
    }
}

@Composable
fun SssRootApp(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    var previewTheme by remember { mutableStateOf(SssNeonTheme.CYBER_CYAN) }
    var skippedSignInForLater by rememberSaveable { mutableStateOf(false) }

    val user = currentUser
    if (user == null && !skippedSignInForLater) {
        SssTheme {
            SssAuthScreen(
                selectedPreviewTheme = previewTheme,
                onSelectPreviewTheme = { previewTheme = it },
                onAuthSuccess = { skippedSignInForLater = false },
                onSkipSignInForLater = { skippedSignInForLater = true }
            )
        }
    } else {
        SssAuthenticatedApp(
            currentUserId = user?.uid ?: "local_sss_user",
            isCloudAuthenticated = user != null,
            initialNeonTheme = previewTheme,
            onRequestSignIn = { skippedSignInForLater = false }
        )
    }
}

@Composable
fun SssAuthenticatedApp(
    currentUserId: String,
    isCloudAuthenticated: Boolean,
    initialNeonTheme: SssNeonTheme,
    onRequestSignIn: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val viewModel: SssMainViewModel = viewModel(
        key = "${currentUserId}_$isCloudAuthenticated",
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                SssMainViewModel(
                    cloudRepository = SssCloudRepository(db),
                    catalogueRepository = MangaCatalogueRepository(),
                    currentUserId = currentUserId,
                    isCloudAuthenticated = isCloudAuthenticated,
                    initialNeonTheme = initialNeonTheme
                )
            }
        }
    )

    val visualConfig by viewModel.visualConfig.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()
    val catalog by viewModel.exploreCatalog.collectAsStateWithLifecycle()
    val isCatalogLoading by viewModel.isCatalogLoading.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedSource by viewModel.selectedSource.collectAsStateWithLifecycle()
    val selectedGenre by viewModel.selectedGenre.collectAsStateWithLifecycle()
    val selectedManga by viewModel.selectedManga.collectAsStateWithLifecycle()
    val chapters by viewModel.chapters.collectAsStateWithLifecycle()
    val activeReadingChapter by viewModel.activeReadingChapter.collectAsStateWithLifecycle()
    val uiProfilesState by viewModel.uiProfilesState.collectAsStateWithLifecycle()
    val libraryState by viewModel.libraryState.collectAsStateWithLifecycle()
    val readingSessionsState by viewModel.readingSessionsState.collectAsStateWithLifecycle()

    var currentTabRoute by rememberSaveable { mutableStateOf("explore") }

    val destinations = listOf(
        SssNavDestination("explore", stringResource(R.string.nav_explore), Icons.Default.Explore),
        SssNavDestination("library", stringResource(R.string.nav_library), Icons.Default.Bookmarks),
        SssNavDestination("studio", stringResource(R.string.nav_studio), Icons.Default.Palette),
        SssNavDestination("analytics", stringResource(R.string.nav_analytics), Icons.Default.Insights)
    )

    if (currentTabRoute != "explore" && selectedManga == null) {
        BackHandler {
            currentTabRoute = "explore"
        }
    }

    SssTheme(visualConfig = visualConfig) {
        val palette = visualConfig.neonTheme
        val useSideRail = visualConfig.navigationBarStyle == "COMPACT_RAIL"

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = palette.background,
            bottomBar = {
                if (!useSideRail && selectedManga == null) {
                    SssAdaptiveBottomBar(
                        destinations = destinations,
                        currentRoute = currentTabRoute,
                        onNavigate = { currentTabRoute = it }
                    )
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useSideRail && selectedManga == null) {
                    SssCompactNavigationRail(
                        destinations = destinations,
                        currentRoute = currentTabRoute,
                        onNavigate = { currentTabRoute = it }
                    )
                }

                Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    when {
                        activeReadingChapter != null && selectedManga != null -> {
                            SssChapterReaderScreen(
                                manga = selectedManga!!,
                                chapter = activeReadingChapter!!,
                                onBackToDetail = { viewModel.closeChapterReader() },
                                onFinishChapterAndLog = {
                                    viewModel.completeChapterAndLogSession(
                                        manga = selectedManga!!,
                                        chapter = activeReadingChapter!!,
                                        minutesSpent = 22
                                    )
                                }
                            )
                        }

                        selectedManga != null -> {
                            MangaDetailScreen(
                                manga = selectedManga!!,
                                chapters = chapters,
                                onBack = { viewModel.closeMangaDetail() },
                                onBookmark = { viewModel.bookmarkManga(it) },
                                onReadChapter = { viewModel.openChapterReader(it) }
                            )
                        }

                        else -> {
                            when (currentTabRoute) {
                                "explore" -> {
                                    ExploreScreen(
                                        catalog = catalog,
                                        isLoading = isCatalogLoading,
                                        searchQuery = searchQuery,
                                        selectedSource = selectedSource,
                                        selectedGenre = selectedGenre,
                                        onSearchChange = { viewModel.updateSearchQuery(it) },
                                        onSourceChange = { viewModel.updateSourceFilter(it) },
                                        onGenreChange = { viewModel.updateGenreFilter(it) },
                                        onRefresh = { viewModel.refreshCatalog() },
                                        onQuickThemeSwitch = { viewModel.selectNeonTheme(it) },
                                        onCycleLayoutMode = {
                                            val next = when (visualConfig.layoutMode) {
                                                "GRID_3" -> "GRID_2"
                                                "GRID_2" -> "COMPACT_LIST"
                                                "COMPACT_LIST" -> "STAGGERED_CARDS"
                                                else -> "GRID_3"
                                            }
                                            viewModel.updateLayoutMode(next)
                                        },
                                        onOpenManga = { viewModel.openMangaDetail(it) },
                                        onQuickBookmark = { viewModel.bookmarkManga(it) },
                                        onOpenStudio = { currentTabRoute = "studio" }
                                    )
                                }

                                "library" -> {
                                    LibraryScreen(
                                        libraryState = libraryState,
                                        catalog = catalog,
                                        onIncrementChapter = { item ->
                                            viewModel.updateBookmarkProgress(
                                                bookmark = item,
                                                newChapter = item.lastReadChapter + 1
                                            )
                                        },
                                        onChangeCategory = { item, newCat ->
                                            viewModel.updateBookmarkProgress(
                                                bookmark = item,
                                                newChapter = item.lastReadChapter,
                                                newCategory = newCat
                                            )
                                        },
                                        onRemoveBookmark = { viewModel.removeBookmark(it) },
                                        onOpenFromCatalog = { bm ->
                                            val match = catalog.firstOrNull { it.id == bm.mangaKey || it.title == bm.title }
                                            if (match != null) {
                                                viewModel.openMangaDetail(match)
                                            }
                                        },
                                        onSeedSampleLibrary = {
                                            catalog.take(3).forEachIndexed { idx, m ->
                                                val cat = if (idx == 0) "FAVORITES" else "READING"
                                                viewModel.bookmarkManga(m, category = cat, lastChapter = (idx + 1) * 5)
                                            }
                                        }
                                    )
                                }

                                "studio" -> {
                                    StudioCustomizationScreen(
                                        uiProfilesState = uiProfilesState,
                                        isCloudAuthenticated = isCloudAuthenticated,
                                        onSelectNeonTheme = { viewModel.selectNeonTheme(it) },
                                        onSelectLayoutMode = { viewModel.updateLayoutMode(it) },
                                        onSelectIconStyle = { viewModel.updateIconStyle(it) },
                                        onSelectNavigationBarStyle = { viewModel.updateNavigationBarStyle(it) },
                                        onSelectHomeSectionOrder = { viewModel.updateHomeSectionOrder(it) },
                                        onCornerRadiusChange = { viewModel.updateCardCornerRadius(it) },
                                        onGlowIntensityChange = { viewModel.updateNeonGlowIntensity(it) },
                                        onReaderModeChange = { viewModel.updateReaderMode(it) },
                                        onSaveCustomPreset = { viewModel.saveCustomPresetToCloud(it) },
                                        onSyncNow = {
                                            if (isCloudAuthenticated) {
                                                viewModel.syncActiveConfigToCloud()
                                            } else {
                                                onRequestSignIn()
                                            }
                                        },
                                        onApplyCloudProfile = { viewModel.applyCloudProfile(it) },
                                        onDeleteCloudProfile = { viewModel.deleteCloudProfile(it) },
                                        onSignOut = {
                                            if (isCloudAuthenticated) {
                                                signOut(
                                                    context = context,
                                                    credentialManager = credentialManager,
                                                    onSignOutComplete = {},
                                                    scope = scope
                                                )
                                            } else {
                                                onRequestSignIn()
                                            }
                                        }
                                    )
                                }

                                "analytics" -> {
                                    AnalyticsScreen(
                                        sessionsState = readingSessionsState,
                                        libraryState = libraryState,
                                        onLogSampleSession = {
                                            val sampleManga = catalog.firstOrNull()
                                            viewModel.logCustomReadingSession(
                                                mangaTitle = sampleManga?.title ?: "SSS-Class Cyber Ronin",
                                                genre = sampleManga?.genre ?: "Cyberpunk",
                                                minutesSpent = 32,
                                                pagesRead = 84,
                                                chaptersCompleted = 2
                                            )
                                        },
                                        onApplyUxRecommendation = { rec ->
                                            viewModel.applyUxRecommendation(rec)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Floating Neon Cloud Sync Notification Toast
                    androidx.compose.animation.AnimatedVisibility(
                        visible = statusBanner != null,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        statusBanner?.let { msg ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(palette.surfaceElevated)
                                    .border(1.5.dp, palette.primaryNeon, RoundedCornerShape(14.dp))
                                    .clickable { viewModel.clearBanner() }
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = palette.primaryNeon,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
