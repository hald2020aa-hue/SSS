package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.MangaBookmark
import com.example.data.model.ReadingSessionLog
import com.example.data.model.UiCustomizationProfile
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class SssRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun saveAndObserveUiProfile_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = SssCloudRepository(firestore)
        val uniqueId = "profile_${UUID.randomUUID().toString().replace("-", "_")}"

        val profile = UiCustomizationProfile(
            userId = uid,
            profileName = "Cyber Samurai Preset",
            neonThemeId = "NEON_MAGENTA",
            layoutMode = "GRID_2",
            iconStyle = "NEON_GLOW",
            navigationBarStyle = "FLOATING_PILL",
            homeSectionOrder = "FEATURED_RECENT_POPULAR",
            cardCornerRadiusDp = 20L,
            neonGlowIntensity = 90L,
            readerMode = "WEBTOON_VERTICAL",
            deviceName = "Galaxy Tab S10",
            isActive = true
        )

        val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveUiProfile(profile, docId = uniqueId)
        }
        assertTrue("Expected saveUiProfile to succeed", saveResult.isSuccess)

        val emitted = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeUiProfiles(uid).first { list -> list.any { it.id == uniqueId } }
        }
        assertTrue(emitted.any { it.id == uniqueId && it.neonThemeId == "NEON_MAGENTA" })
    }

    @Test
    fun saveMangaBookmarkAndSession_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = SssCloudRepository(firestore)
        val mangaDocId = "manga_${UUID.randomUUID().toString().replace("-", "_")}"

        val bookmark = MangaBookmark(
            userId = uid,
            mangaKey = mangaDocId,
            title = "Omniscient Reader's Viewpoint",
            coverUrl = "https://uploads.mangadex.org/covers/sample.jpg",
            sourceName = "MangaDex",
            author = "Sing N Song",
            genre = "Fantasy",
            category = "READING",
            lastReadChapter = 180L,
            totalChapters = 220L,
            rating = 4.9
        )

        val bookmarkResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveMangaBookmark(bookmark, customId = mangaDocId)
        }
        assertTrue(bookmarkResult.isSuccess)

        val sessionResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.logReadingSession(
                ReadingSessionLog(
                    userId = uid,
                    mangaTitle = "Omniscient Reader's Viewpoint",
                    genre = "Fantasy",
                    minutesSpent = 35L,
                    pagesRead = 95L,
                    chaptersCompleted = 2L,
                    neonThemeUsed = "ELECTRIC_VIOLET",
                    layoutModeUsed = "GRID_3"
                )
            )
        }
        assertTrue(sessionResult.isSuccess)
    }

    @Test
    fun crossUserRead_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = SssCloudRepository(firestore)
        val profileId = "profile_${UUID.randomUUID().toString().replace("-", "_")}"

        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.saveUiProfile(
                UiCustomizationProfile(userId = aliceUid, profileName = "Alice Private UI"),
                docId = profileId
            ).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = SssCloudRepository(firestore)
        val crossReadResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getUiProfileById(aliceUid, profileId)
        }
        assertTrue("Expected cross-user read to fail", crossReadResult.isFailure)
        val firestoreEx = crossReadResult.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull(firestoreEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
    }

    @Test
    fun unauthenticatedObserve_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        auth.signOut()
        val repository = SssCloudRepository(firestore)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeUiProfiles(aliceUid).first()
            }
            fail("Expected unauthenticated observation to fail")
        } catch (e: Exception) {
            val firestoreEx = e.unwrapFirestoreException()
            assertNotNull("Expected FirebaseFirestoreException cause but got $e", firestoreEx)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice_sss@test.com"
        const val BOB_EMAIL = "bob_sss@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
