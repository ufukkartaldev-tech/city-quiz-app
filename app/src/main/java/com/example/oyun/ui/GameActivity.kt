package com.example.oyun.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.oyun.data.Question
import com.example.oyun.data.QuestionManager
import com.example.oyun.databinding.ActivityGameBinding
import com.example.oyun.ui.game.GameViewModel
import com.example.oyun.managers.SoundManager
import com.example.oyun.managers.TimeManager
import com.example.oyun.managers.UIManager
import com.example.oyun.ui.game.AnswerHandler
import com.example.oyun.ui.game.GameStateObserver
import com.example.oyun.ui.game.JokerHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameBinding
    private lateinit var uiManager: UIManager
    private lateinit var timerManager: TimeManager
    private lateinit var answerHandler: AnswerHandler
    private lateinit var jokerHandler: JokerHandler
    private lateinit var gameStateObserver: GameStateObserver

    @Inject
    lateinit var soundManager: SoundManager
    @Inject
    lateinit var prefs: SharedPreferences
    @Inject
    lateinit var questionManager: QuestionManager

    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeManagers()
        setupJokerButtons()
        setupObservers()

        if (savedInstanceState == null) {
            val startLevel = intent.getIntExtra("LEVEL", 1)
            val isNewGame = intent.getBooleanExtra("IS_NEW_GAME", true)
            gameViewModel.startGame(startLevel, isNewGame)
        }
    }

    private fun initializeManagers() {
        uiManager = UIManager(this, binding)
        timerManager = TimeManager(uiManager)
        
        answerHandler = AnswerHandler(
            gameViewModel = gameViewModel,
            soundManager = soundManager,
            timerManager = timerManager,
            onAnswerProcessed = ::handleAnswerProcessed
        )
        
        jokerHandler = JokerHandler(
            context = this,
            binding = binding,
            gameViewModel = gameViewModel,
            questionManager = questionManager,
            uiManager = uiManager,
            timerManager = timerManager
        )
    }

    private fun setupObservers() {
        gameStateObserver = GameStateObserver(
            lifecycleOwner = this,
            gameViewModel = gameViewModel,
            uiManager = uiManager,
            timerManager = timerManager,
            maxLives = MAX_LIVES,
            onGameOver = ::navigateToGameOver,
            onLevelUp = ::handleLevelUp,
            onAnswerRequired = ::handleAnswerSelected,
            onTimeUp = ::handleTimeUp
        )
        gameStateObserver.observe()
    }

    private fun setupJokerButtons() {
        binding.jokerFiftyFifty.setOnClickListener {
            jokerHandler.applyFiftyFifty(gameViewModel.uiState.value.currentQuestion)
        }
        
        binding.jokerSkip.setOnClickListener {
            jokerHandler.useSkipJoker {
                // Soru atlandı
            }
        }
        
        binding.jokerGainLife.setOnClickListener {
            jokerHandler.useGainLifeJoker { isCorrect ->
                answerHandler.processAnswer(isCorrect)
            }
        }
    }

    private fun handleAnswerSelected(question: Question, selectedIndex: Int) {
        val isCorrect = selectedIndex == question.correctAnswerIndex
        answerHandler.processAnswer(isCorrect)
    }

    private fun handleTimeUp() {
        answerHandler.processAnswer(false)
    }

    private fun handleAnswerProcessed() {
        lifecycleScope.launch {
            delay(ANSWER_DELAY)
            val state = gameViewModel.uiState.value
            
            // Oyun devam ediyorsa yeni soru yükle
            if (state.lives > 0 && state.questionsAnsweredInLevel < QUESTIONS_PER_LEVEL) {
                gameViewModel.loadNextQuestion()
            }
        }
    }

    private fun handleLevelUp(newLevel: Int) {
        uiManager.showLevelUpMessage(newLevel)
        lifecycleScope.launch {
            delay(LEVEL_UP_DELAY)
            gameViewModel.loadNextQuestion()
        }
    }

    private fun navigateToGameOver() {
        timerManager.stopTimer()
        val uiState = gameViewModel.uiState.value
        
        val intent = Intent(this, GameOverActivity::class.java).apply {
            putExtra("score", uiState.score)
            putExtra("correct_answers", uiState.score / 10)
            putExtra("total_questions", uiState.questionsAnsweredInLevel)
            putExtra("level", uiState.currentLevel)
            // Joker bilgilerini de gönder
            putExtra("joker_fiftyfifty", uiState.jokerInventory.fiftyFifty)
            putExtra("joker_skip", uiState.jokerInventory.skip)
            putExtra("joker_gainlife", uiState.jokerInventory.gainLife)
        }
        
        startActivity(intent)
        finish()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        timerManager.stopTimer()
    }

    companion object {
        private const val MAX_LIVES = 5
        private const val QUESTIONS_PER_LEVEL = 10
        private const val ANSWER_DELAY = 1000L
        private const val LEVEL_UP_DELAY = 1500L
    }
}
