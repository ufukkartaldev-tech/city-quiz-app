package com.example.oyun.ui.multiplayer

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.oyun.data.Question
import com.google.firebase.firestore.*
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * MultiplayerViewModel Unit Tests
 * 
 * Test Coverage:
 * - Game room creation
 * - Player matching
 * - Real-time sync
 * - Score updates
 * - Game state management
 */
@ExperimentalCoroutinesApi
class MultiplayerViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: MultiplayerViewModel
    private lateinit var mockFirestore: FirebaseFirestore
    private lateinit var mockGameRoomsCollection: CollectionReference
    private val testDispatcher = StandardTestDispatcher()

    private val testUserId = "test_user_123"
    private val testRoomId = "room_456"

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        mockFirestore = mockk(relaxed = true)
        mockGameRoomsCollection = mockk(relaxed = true)
        
        every { mockFirestore.collection("game_rooms") } returns mockGameRoomsCollection

        viewModel = MultiplayerViewModel(mockFirestore, testUserId)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    // ============================================
    // GAME ROOM CREATION TESTS
    // ============================================

    @Test
    fun `createGameRoom should create room with host`() = runTest {
        // Given
        val mockDocRef = mockk<DocumentReference>()
        every { mockDocRef.id } returns testRoomId
        every { mockGameRoomsCollection.add(any()) } returns com.google.android.gms.tasks.Tasks.forResult(mockDocRef)

        // When
        viewModel.createGameRoom()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { mockGameRoomsCollection.add(match<Map<String, Any>> {
            it["hostUid"] == testUserId &&
            it["status"] == "WAITING" &&
            (it["players"] as? List<*>)?.size == 1
        }) }
    }

    @Test
    fun `createGameRoom should set initial game state`() = runTest {
        // Given
        val mockDocRef = mockk<DocumentReference>()
        every { mockDocRef.id } returns testRoomId
        every { mockGameRoomsCollection.add(any()) } returns com.google.android.gms.tasks.Tasks.forResult(mockDocRef)

        // When
        viewModel.createGameRoom()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { mockGameRoomsCollection.add(match<Map<String, Any>> {
            it["currentQuestionIndex"] == 0 &&
            it["hostScore"] == 0 &&
            it["guestScore"] == 0
        }) }
    }

    // ============================================
    // PLAYER MATCHING TESTS
    // ============================================

    @Test
    fun `joinGameRoom should add player to room`() = runTest {
        // Given
        val mockDocRef = mockk<DocumentReference>()
        every { mockGameRoomsCollection.document(testRoomId) } returns mockDocRef
        every { mockDocRef.update(any<Map<String, Any>>()) } returns com.google.android.gms.tasks.Tasks.forResult(null)

        // When
        viewModel.joinGameRoom(testRoomId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { mockDocRef.update(match<Map<String, Any>> {
            it.containsKey("players")
        }) }
    }

    @Test
    fun `joinGameRoom should update status to PLAYING when 2 players`() = runTest {
        // Given
        val mockDocRef = mockk<DocumentReference>()
        every { mockGameRoomsCollection.document(testRoomId) } returns mockDocRef
        every { mockDocRef.update(any<Map<String, Any>>()) } returns com.google.android.gms.tasks.Tasks.forResult(null)

        // When
        viewModel.joinGameRoom(testRoomId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { mockDocRef.update(match<Map<String, Any>> {
            it["status"] == "PLAYING"
        }) }
    }

    // ============================================
    // SCORE UPDATE TESTS
    // ============================================

    @Test
    fun `updateScore should update host score when host answers`() = runTest {
        // Given
        val isHost = true
        val newScore = 100
        val mockDocRef = mockk<DocumentReference>()
        every { mockGameRoomsCollection.document(testRoomId) } returns mockDocRef
        every { mockDocRef.update(any<String>(), any()) } returns com.google.android.gms.tasks.Tasks.forResult(null)

        // When
        viewModel.updateScore(testRoomId, isHost, newScore)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { mockDocRef.update("hostScore", newScore) }
    }

    @Test
    fun `updateScore should update guest score when guest answers`() = runTest {
        // Given
        val isHost = false
        val newScore = 150
        val mockDocRef = mockk<DocumentReference>()
        every { mockGameRoomsCollection.document(testRoomId) } returns mockDocRef
        every { mockDocRef.update(any<String>(), any()) } returns com.google.android.gms.tasks.Tasks.forResult(null)

        // When
        viewModel.updateScore(testRoomId, isHost, newScore)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { mockDocRef.update("guestScore", newScore) }
    }

    // ============================================
    // GAME STATE TESTS
    // ============================================

    @Test
    fun `nextQuestion should increment question index`() = runTest {
        // Given
        val currentIndex = 5
        val mockDocRef = mockk<DocumentReference>()
        every { mockGameRoomsCollection.document(testRoomId) } returns mockDocRef
        every { mockDocRef.update(any<String>(), any()) } returns com.google.android.gms.tasks.Tasks.forResult(null)

        // When
        viewModel.nextQuestion(testRoomId, currentIndex)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { mockDocRef.update("currentQuestionIndex", currentIndex + 1) }
    }

    @Test
    fun `endGame should update status to FINISHED`() = runTest {
        // Given
        val mockDocRef = mockk<DocumentReference>()
        every { mockGameRoomsCollection.document(testRoomId) } returns mockDocRef
        every { mockDocRef.update(any<String>(), any()) } returns com.google.android.gms.tasks.Tasks.forResult(null)

        // When
        viewModel.endGame(testRoomId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { mockDocRef.update("status", "FINISHED") }
    }

    // ============================================
    // REAL-TIME SYNC TESTS
    // ============================================

    @Test
    fun `observeGameRoom should listen to room changes`() = runTest {
        // Given
        val mockDocRef = mockk<DocumentReference>()
        val mockListenerRegistration = mockk<ListenerRegistration>(relaxed = true)
        
        every { mockGameRoomsCollection.document(testRoomId) } returns mockDocRef
        every { mockDocRef.addSnapshotListener(any()) } returns mockListenerRegistration

        // When
        viewModel.observeGameRoom(testRoomId)

        // Then
        verify { mockDocRef.addSnapshotListener(any()) }
    }

    @Test
    fun `leaveGameRoom should remove player from room`() = runTest {
        // Given
        val mockDocRef = mockk<DocumentReference>()
        every { mockGameRoomsCollection.document(testRoomId) } returns mockDocRef
        every { mockDocRef.update(any<Map<String, Any>>()) } returns com.google.android.gms.tasks.Tasks.forResult(null)

        // When
        viewModel.leaveGameRoom(testRoomId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { mockDocRef.update(any<Map<String, Any>>()) }
    }
}
