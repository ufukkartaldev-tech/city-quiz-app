package com.example.oyun.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.oyun.R
import com.example.oyun.data.GameRoom
import com.example.oyun.databinding.ActivityMultiplayerGameBinding
import com.example.oyun.ui.multiplayer.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MultiplayerGameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMultiplayerGameBinding
    private lateinit var uiManager: MultiplayerUIManager
    private lateinit var timer: MultiplayerTimer
    private lateinit var roomManager: RoomManager
    private lateinit var gameOverDialog: GameOverDialogManager

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId: String
        get() = auth.currentUser?.uid ?: ""

    private var roomId: String = ""
    private var isHost: Boolean = false
    private var currentQuestionIndex = 0
    private var isGameFinishedForMe = false
    
    // Soruları başlangıçta karıştır ve sakla (her iki oyuncu için aynı sıra)
    // Seed ile initialize edilecek
    private var questions: List<com.example.oyun.data.MultiplayerQuestion> = emptyList()
    private var areQuestionsInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMultiplayerGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!validateIntent()) return

        initializeManagers()
        setupClickListeners()
        roomManager.startListening()
    }

    private fun validateIntent(): Boolean {
        roomId = intent.getStringExtra("ROOM_ID") ?: ""
        isHost = intent.getBooleanExtra("IS_HOST", false)

        if (roomId.isEmpty()) {
            Toast.makeText(this, getString(R.string.room_error), Toast.LENGTH_SHORT).show()
            finish()
            return false
        }
        return true
    }

    private fun initializeManagers() {
        uiManager = MultiplayerUIManager(this, binding)
        gameOverDialog = GameOverDialogManager(this)
        
        timer = MultiplayerTimer(
            binding = binding,
            timePerQuestion = TIME_PER_QUESTION,
            onTimeUp = ::handleTimeUp
        )
        
        roomManager = RoomManager(
            db = db,
            roomId = roomId,
            userId = userId,
            isHost = isHost,
            onRoomUpdate = ::handleRoomUpdate,
            onError = ::handleRoomError
        )
    }

    private var lastProcessedEmojiId: String? = null

    // ... (existing code)

    private fun setupClickListeners() {
        binding.answerButton1.setOnClickListener { checkAnswer(0) }
        binding.answerButton2.setOnClickListener { checkAnswer(1) }
        binding.answerButton3.setOnClickListener { checkAnswer(2) }
        binding.answerButton4.setOnClickListener { checkAnswer(3) }
        
        binding.emojiButton.setOnClickListener {
            showEmojiDialog()
        }
    }

    private fun handleRoomUpdate(room: GameRoom) {
        updatePlayerUI(room)
        handleGameState(room)
        checkAndShowEmoji(room)
        
        // Show room code if waiting and private
        if (room.status == "WAITING" && room.isPrivate && !room.roomCode.isNullOrEmpty()) {
            binding.roomCodeText.visibility = android.view.View.VISIBLE
            binding.roomCodeText.text = "Oda Kodu: ${room.roomCode}"
        } else {
            binding.roomCodeText.visibility = android.view.View.GONE
        }
    }

    private fun checkAndShowEmoji(room: GameRoom) {
        val currentEmoji = room.lastEmoji ?: return
        if (currentEmoji != lastProcessedEmojiId) {
            lastProcessedEmojiId = currentEmoji
            
            val parts = currentEmoji.split("|")
            if (parts.size >= 2) {
                val senderId = parts[0]
                val emoji = parts[1]
                
                // Sadece rakip gönderdiyse göster
                if (senderId != userId) {
                    showEmojiAnimation(emoji)
                }
            }
        }
    }

    private fun showEmojiDialog() {
        val emojis = arrayOf("👏", "😱", "😂", "😡", "👍", "🤔")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Bir Emoji Seç")
            .setItems(emojis) { _, which ->
                roomManager.sendEmoji(emojis[which])
            }
            .show()
    }

    private fun showEmojiAnimation(emoji: String) {
        binding.emojiOverlayText.text = emoji
        binding.emojiOverlayText.visibility = android.view.View.VISIBLE
        binding.emojiOverlayText.alpha = 0f
        binding.emojiOverlayText.scaleX = 0.5f
        binding.emojiOverlayText.scaleY = 0.5f

        binding.emojiOverlayText.animate()
            .alpha(1f)
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(300)
            .withEndAction {
                binding.emojiOverlayText.animate()
                    .alpha(0f)
                    .scaleX(0.5f)
                    .scaleY(0.5f)
                    .setStartDelay(1000)
                    .setDuration(300)
                    .withEndAction {
                        binding.emojiOverlayText.visibility = android.view.View.GONE
                    }
                    .start()
            }
            .start()
    }

    // ... (rest of existing code)

    private fun updatePlayerUI(room: GameRoom) {
        val (myName, myScore, opponentName, opponentScore) = if (room.hostId == userId) {
            PlayerScores(
                room.hostName,
                room.hostScore,
                room.guestName ?: getString(R.string.waiting_opponent),
                room.guestScore
            )
        } else {
            PlayerScores(
                room.guestName ?: getString(R.string.guest),
                room.guestScore,
                room.hostName,
                room.hostScore
            )
        }
        
        uiManager.updatePlayerScores(myName, myScore, opponentName, opponentScore)
    }

    private fun handleGameState(room: GameRoom) {
        when (room.status) {
            "WAITING" -> {
                uiManager.showWaitingForOpponent()
            }
            "PLAYING" -> {
                // Soruları seed ile initialize et (sadece bir kere)
                if (!areQuestionsInitialized) {
                    val random = java.util.Random(room.questionSeed)
                    questions = MultiplayerQuestions.questionSet.map { it.shuffled(random) }
                    areQuestionsInitialized = true
                }

                if (shouldStartGame()) {
                    loadQuestion(0)
                }
                
                if (room.hostFinished && room.guestFinished) {
                    showGameOver(room)
                }
            }
        }
    }

    private fun shouldStartGame(): Boolean {
        return currentQuestionIndex == 0 && 
               binding.questionText.text == getString(R.string.waiting_opponent)
    }

    private fun loadQuestion(index: Int) {
        if (index >= questions.size) {
            finishGameForMe()
            return
        }

        // Sorular zaten başlangıçta karıştırılmış
        val question = questions[index]
        uiManager.displayQuestion(question, index)
        timer.start()
    }

    private fun handleTimeUp() {
        if (uiManager.areAnswerButtonsEnabled()) {
            checkAnswer(-1) // Süre doldu, yanlış cevap
        }
    }

    private fun checkAnswer(selectedIndex: Int) {
        timer.cancel()
        uiManager.setAnswerButtonsEnabled(false)

        val currentQuestion = questions[currentQuestionIndex]
        val isCorrect = selectedIndex == currentQuestion.correctAnswerIndex
        val score = calculateScore(isCorrect)

        displayAnswerFeedback(isCorrect, score)
        roomManager.updateScore(score)

        // Hızlıca sonraki soruya geç
        binding.root.postDelayed({
            currentQuestionIndex++
            loadQuestion(currentQuestionIndex)
        }, ANSWER_FEEDBACK_DELAY)
    }

    private fun calculateScore(isCorrect: Boolean): Int {
        return if (isCorrect) {
            BASE_SCORE + ((timer.getTimeLeft() / 1000) * TIME_BONUS).toInt()
        } else {
            0
        }
    }

    private fun displayAnswerFeedback(isCorrect: Boolean, score: Int) {
        if (isCorrect) {
            uiManager.showCorrectAnswer(score)
        } else {
            uiManager.showWrongAnswer()
        }
    }

    private fun finishGameForMe() {
        isGameFinishedForMe = true
        uiManager.showGameFinished()
        roomManager.markAsFinished()
    }

    private fun showGameOver(room: GameRoom) {
        if (!isFinishing) {
            gameOverDialog.showGameOverDialog(room) {
                finish()
            }
        }
    }

    private fun handleRoomError(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
        roomManager.stopListening()
    }

    private data class PlayerScores(
        val myName: String,
        val myScore: Int,
        val opponentName: String,
        val opponentScore: Int
    )

    companion object {
        private const val TIME_PER_QUESTION = 15000L // 15 saniye
        private const val ANSWER_FEEDBACK_DELAY = 500L
        private const val BASE_SCORE = 100
        private const val TIME_BONUS = 10
    }
}
