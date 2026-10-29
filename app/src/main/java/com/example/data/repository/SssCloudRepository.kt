package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.MangaBookmark
import com.example.data.model.ReadingSessionLog
import com.example.data.model.UiCustomizationProfile
import com.example.data.util.OperationType
import com.example.data.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class SssCloudRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // --- UI CUSTOMIZATION PROFILES ---

    fun observeUiProfiles(userId: String = requireUserId()): Flow<List<UiCustomizationProfile>> = flow {
        val path = "users/$userId/ui_profiles"
        emitAll(
            db.collection("users").document(userId).collection("ui_profiles")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        UiCustomizationProfile::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun saveUiProfile(profile: UiCustomizationProfile, docId: String? = null): Result<String> {
        val uid = requireUserId()
        val collectionRef = db.collection("users").document(uid).collection("ui_profiles")
        val targetDocId = docId?.takeIf { it.isNotBlank() } ?: profile.id.takeIf { it.isNotBlank() }
        return try {
            if (targetDocId != null && profile.createdAt != null) {
                val docRef = collectionRef.document(targetDocId)
                val updatePayload = mapOf(
                    "profileName" to profile.profileName.take(80),
                    "neonThemeId" to profile.neonThemeId,
                    "layoutMode" to profile.layoutMode,
                    "iconStyle" to profile.iconStyle,
                    "navigationBarStyle" to profile.navigationBarStyle,
                    "homeSectionOrder" to profile.homeSectionOrder,
                    "cardCornerRadiusDp" to profile.cardCornerRadiusDp.coerceIn(4L, 32L),
                    "neonGlowIntensity" to profile.neonGlowIntensity.coerceIn(0L, 100L),
                    "readerMode" to profile.readerMode,
                    "deviceName" to profile.deviceName.take(80),
                    "isActive" to profile.isActive,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updatePayload).await()
                Result.success(targetDocId)
            } else {
                val docRef = if (targetDocId != null) collectionRef.document(targetDocId) else collectionRef.document()
                val createPayload = mapOf(
                    "userId" to uid,
                    "profileName" to profile.profileName.take(80).ifBlank { "SSS Profile" },
                    "neonThemeId" to profile.neonThemeId,
                    "layoutMode" to profile.layoutMode,
                    "iconStyle" to profile.iconStyle,
                    "navigationBarStyle" to profile.navigationBarStyle,
                    "homeSectionOrder" to profile.homeSectionOrder,
                    "cardCornerRadiusDp" to profile.cardCornerRadiusDp.coerceIn(4L, 32L),
                    "neonGlowIntensity" to profile.neonGlowIntensity.coerceIn(0L, 100L),
                    "readerMode" to profile.readerMode,
                    "deviceName" to profile.deviceName.take(80).ifBlank { "Android Device" },
                    "isActive" to profile.isActive,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(createPayload).await()
                Result.success(docRef.id)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, collectionRef.path)
            Result.failure(e)
        }
    }

    suspend fun getUiProfileById(targetUserId: String, profileId: String): Result<UiCustomizationProfile?> {
        val docRef = db.collection("users").document(targetUserId).collection("ui_profiles").document(profileId)
        return try {
            val snap = docRef.get().await()
            Result.success(snap.toObject(UiCustomizationProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteUiProfile(profileId: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("ui_profiles").document(profileId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // --- MANGA LIBRARY BOOKMARKS ---

    fun observeLibrary(userId: String = requireUserId()): Flow<List<MangaBookmark>> = flow {
        val path = "users/$userId/library"
        emitAll(
            db.collection("users").document(userId).collection("library")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        MangaBookmark::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun saveMangaBookmark(bookmark: MangaBookmark, customId: String? = null): Result<String> {
        val uid = requireUserId()
        val collectionRef = db.collection("users").document(uid).collection("library")
        val sanitizedKey = (customId ?: bookmark.id.ifBlank { bookmark.mangaKey })
            .replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
            .take(100)
            .ifBlank { "manga_${System.currentTimeMillis()}" }
        val docRef = collectionRef.document(sanitizedKey)
        return try {
            if (bookmark.createdAt != null && bookmark.id.isNotBlank()) {
                val updatePayload = mapOf(
                    "mangaKey" to bookmark.mangaKey.take(128).ifBlank { sanitizedKey },
                    "title" to bookmark.title.take(200).ifBlank { "Untitled Manga" },
                    "coverUrl" to bookmark.coverUrl.take(1000),
                    "sourceName" to bookmark.sourceName.take(80).ifBlank { "MangaDex" },
                    "author" to bookmark.author.take(120),
                    "genre" to bookmark.genre.take(80),
                    "category" to bookmark.category,
                    "lastReadChapter" to bookmark.lastReadChapter.coerceIn(0L, 10000L),
                    "totalChapters" to bookmark.totalChapters.coerceIn(0L, 10000L),
                    "rating" to bookmark.rating.coerceIn(0.0, 5.0),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updatePayload).await()
            } else {
                val createPayload = mapOf(
                    "userId" to uid,
                    "mangaKey" to bookmark.mangaKey.take(128).ifBlank { sanitizedKey },
                    "title" to bookmark.title.take(200).ifBlank { "Untitled Manga" },
                    "coverUrl" to bookmark.coverUrl.take(1000),
                    "sourceName" to bookmark.sourceName.take(80).ifBlank { "MangaDex" },
                    "author" to bookmark.author.take(120),
                    "genre" to bookmark.genre.take(80),
                    "category" to bookmark.category,
                    "lastReadChapter" to bookmark.lastReadChapter.coerceIn(0L, 10000L),
                    "totalChapters" to bookmark.totalChapters.coerceIn(0L, 10000L),
                    "rating" to bookmark.rating.coerceIn(0.0, 5.0),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(createPayload).await()
            }
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun removeMangaBookmark(mangaDocId: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("library").document(mangaDocId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // --- READING SESSIONS & ANALYTICS ---

    fun observeReadingSessions(userId: String = requireUserId()): Flow<List<ReadingSessionLog>> = flow {
        val path = "users/$userId/reading_sessions"
        emitAll(
            db.collection("users").document(userId).collection("reading_sessions")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        ReadingSessionLog::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun logReadingSession(session: ReadingSessionLog): Result<String> {
        val uid = requireUserId()
        val collectionRef = db.collection("users").document(uid).collection("reading_sessions")
        val docRef = collectionRef.document()
        return try {
            val payload = mapOf(
                "userId" to uid,
                "mangaTitle" to session.mangaTitle.take(200).ifBlank { "Manga Chapter" },
                "genre" to session.genre.take(80).ifBlank { "Action" },
                "minutesSpent" to session.minutesSpent.coerceIn(1L, 1440L),
                "pagesRead" to session.pagesRead.coerceIn(1L, 5000L),
                "chaptersCompleted" to session.chaptersCompleted.coerceIn(0L, 500L),
                "neonThemeUsed" to session.neonThemeUsed.take(40).ifBlank { "CYBER_CYAN" },
                "layoutModeUsed" to session.layoutModeUsed.take(40).ifBlank { "GRID_3" },
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, collectionRef.path)
            Result.failure(e)
        }
    }
}
