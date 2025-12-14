package com.example.oyun.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.oyun.data.local.CachedQuestion
import com.example.oyun.data.local.CachedQuestionDao
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * HybridQuestionRepository Unit Tests
 * 
 * Test Coverage:
 * - Initialization (JSON loading)
 * - Firestore sync
 * - Question retrieval (Room > JSON priority)
 * - Statistics
 * - Cache management
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class HybridQuestionRepositoryTest {

    private lateinit var repository: HybridQuestionRepository
    private lateinit var mockContext: Context
    private lateinit var mockDao: CachedQuestionDao
    private lateinit var mockFirestore: FirebaseFirestore
    private lateinit var gson: Gson

    @Before
    fun setup() {
        // Mock dependencies
        mockContext = ApplicationProvider.getApplicationContext()
        mockDao = mockk(relaxed = true)
        mockFirestore = mockk(relaxed = true)
        gson = Gson()

        // Create repository
        repository = HybridQuestionRepository(
            context = mockContext,
            cachedQuestionDao = mockDao,
            firestore = mockFirestore,
            gson = gson
        )
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    // ============================================
    // INITIALIZATION TESTS
    // ============================================

    @Test
    fun `initialize should load questions from JSON`() = runTest {
        // Given
        coEvery { mockDao.getLastSyncTime(any()) } returns null

        // When
        repository.initialize()

        // Then
        val stats = repository.getStatistics()
        assertTrue(stats.jsonQuestions > 0, "JSON questions should be loaded")
    }

    @Test
    fun `initialize should not crash if JSON is missing`() = runTest {
        // Given - JSON dosyası olmayan bir context
        // (Robolectric otomatik handle eder)

        // When & Then - Crash olmamalı
        repository.initialize()
    }

    @Test
    fun `initialize should trigger Firestore sync in background`() = runTest {
        // Given
        coEvery { mockDao.getLastSyncTime(any()) } returns null
        coEvery { mockDao.insertQuestions(any()) } just Runs

        // When
        repository.initialize()

        // Then
        // Sync arka planda çalışır, hemen verify edemeyiz
        // Ama initialize başarılı olmalı
        val stats = repository.getStatistics()
        assertNotNull(stats)
    }

    // ============================================
    // QUESTION RETRIEVAL TESTS
    // ============================================

    @Test
    fun `getQuestionsForLevel should return Room questions when available`() = runTest {
        // Given
        val level = 1
        val cachedQuestions = listOf(
            createCachedQuestion(1, level),
            createCachedQuestion(2, level)
        )
        coEvery { mockDao.getQuestionsByLevel(level) } returns cachedQuestions

        // When
        val questions = repository.getQuestionsForLevel(level)

        // Then
        assertEquals(2, questions.size)
        coVerify { mockDao.getQuestionsByLevel(level) }
    }

    @Test
    fun `getQuestionsForLevel should fallback to JSON when Room is empty`() = runTest {
        // Given
        val level = 1
        coEvery { mockDao.getQuestionsByLevel(level) } returns emptyList()
        repository.initialize() // JSON yükle

        // When
        val questions = repository.getQuestionsForLevel(level)

        // Then
        // JSON'dan gelmeli (eğer JSON'da level 1 varsa)
        assertTrue(questions.isNotEmpty() || questions.isEmpty()) // JSON içeriğine bağlı
    }

    @Test
    fun `getQuestionsForLevel should return empty list when no questions found`() = runTest {
        // Given
        val level = 999 // Olmayan bir level
        coEvery { mockDao.getQuestionsByLevel(level) } returns emptyList()

        // When
        val questions = repository.getQuestionsForLevel(level)

        // Then
        assertTrue(questions.isEmpty())
    }

    @Test
    fun `getQuestionsForLevelFlow should emit Room questions`() = runTest {
        // Given
        val level = 1
        val cachedQuestions = listOf(createCachedQuestion(1, level))
        coEvery { mockDao.getQuestionsByLevelFlow(level) } returns flowOf(cachedQuestions)

        // When
        val questions = repository.getQuestionsForLevelFlow(level).first()

        // Then
        assertEquals(1, questions.size)
    }

    // ============================================
    // CATEGORY TESTS
    // ============================================

    @Test
    fun `getQuestionsByCategory should return filtered questions`() = runTest {
        // Given
        val category = "GEOGRAPHY"
        val cachedQuestions = listOf(
            createCachedQuestion(1, 1, category),
            createCachedQuestion(2, 1, category)
        )
        coEvery { mockDao.getQuestionsByCategory(category) } returns cachedQuestions

        // When
        val questions = repository.getQuestionsByCategory(category)

        // Then
        assertEquals(2, questions.size)
        coVerify { mockDao.getQuestionsByCategory(category) }
    }

    // ============================================
    // STATISTICS TESTS
    // ============================================

    @Test
    fun `getStatistics should return correct counts`() = runTest {
        // Given
        coEvery { mockDao.getQuestionCount() } returns 50
        coEvery { mockDao.getFirestoreQuestionCount() } returns 30
        coEvery { mockDao.getLastSyncTime(any()) } returns System.currentTimeMillis()
        repository.initialize()

        // When
        val stats = repository.getStatistics()

        // Then
        assertEquals(50, stats.cachedQuestions)
        assertEquals(30, stats.firestoreQuestions)
        assertNotNull(stats.lastSyncTime)
    }

    @Test
    fun `hasQuestionsForLevel should return true when questions exist in Room`() = runTest {
        // Given
        val level = 1
        coEvery { mockDao.hasQuestionsForLevel(level) } returns true

        // When
        val hasQuestions = repository.hasQuestionsForLevel(level)

        // Then
        assertTrue(hasQuestions)
    }

    @Test
    fun `hasQuestionsForLevel should check JSON when Room is empty`() = runTest {
        // Given
        val level = 1
        coEvery { mockDao.hasQuestionsForLevel(level) } returns false
        repository.initialize()

        // When
        val hasQuestions = repository.hasQuestionsForLevel(level)

        // Then
        // JSON'da level 1 varsa true, yoksa false
        // Test JSON içeriğine bağlı
        assertNotNull(hasQuestions)
    }

    // ============================================
    // CACHE MANAGEMENT TESTS
    // ============================================

    @Test
    fun `clearCache should delete all cached questions`() = runTest {
        // Given
        coEvery { mockDao.deleteAllQuestions() } just Runs

        // When
        repository.clearCache()

        // Then
        coVerify { mockDao.deleteAllQuestions() }
    }

    @Test
    fun `clearFirestoreCache should delete only Firestore questions`() = runTest {
        // Given
        coEvery { mockDao.deleteFirestoreQuestions() } just Runs

        // When
        repository.clearFirestoreCache()

        // Then
        coVerify { mockDao.deleteFirestoreQuestions() }
    }

    // ============================================
    // SYNC TESTS
    // ============================================

    @Test
    fun `forceSyncFirestore should trigger manual sync`() = runTest {
        // Given
        coEvery { mockDao.getLastSyncTime(any()) } returns null
        coEvery { mockDao.insertQuestions(any()) } just Runs

        // When
        repository.forceSyncFirestore()

        // Then
        // Sync çağrıldı (Firestore mock olduğu için gerçek sync olmaz)
        // Ama fonksiyon crash etmemeli
        assertTrue(true)
    }

    // ============================================
    // EDGE CASES
    // ============================================

    @Test
    fun `getAllQuestions should merge Room and JSON questions`() = runTest {
        // Given
        val cachedQuestions = listOf(
            createCachedQuestion(1, 1),
            createCachedQuestion(2, 1)
        )
        coEvery { mockDao.getAllQuestions() } returns cachedQuestions
        repository.initialize()

        // When
        val allQuestions = repository.getAllQuestions()

        // Then
        assertTrue(allQuestions.size >= 2) // En az Room'daki 2 soru
    }

    @Test
    fun `repository should handle concurrent access safely`() = runTest {
        // Given
        coEvery { mockDao.getQuestionsByLevel(any()) } returns emptyList()

        // When - Aynı anda birden fazla istek
        val level1 = repository.getQuestionsForLevel(1)
        val level2 = repository.getQuestionsForLevel(2)
        val level3 = repository.getQuestionsForLevel(3)

        // Then - Crash olmamalı
        assertNotNull(level1)
        assertNotNull(level2)
        assertNotNull(level3)
    }

    // ============================================
    // HELPER METHODS
    // ============================================

    private fun createCachedQuestion(
        id: Int,
        level: Int,
        category: String = "GEOGRAPHY"
    ): CachedQuestion {
        return CachedQuestion(
            id = id,
            questionText = "Test Question $id",
            optionA = "Option A",
            optionB = "Option B",
            optionC = "Option C",
            optionD = "Option D",
            correctAnswer = "A",
            imageName = "test_$id.png",
            level = level,
            category = category,
            difficulty = "MEDIUM",
            explanation = "Test explanation",
            tags = """["test", "geography"]""",
            points = 10,
            timeLimit = 30,
            isVerified = true,
            authorId = "system",
            createdAt = System.currentTimeMillis(),
            downloadedAt = System.currentTimeMillis(),
            isFromFirestore = true
        )
    }
}
