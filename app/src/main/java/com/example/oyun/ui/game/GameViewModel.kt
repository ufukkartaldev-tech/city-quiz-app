package com.example.oyun.ui.game

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.example.oyun.data.Question
import com.example.oyun.data.QuestionRepository
import com.example.oyun.managers.AchievementManager
import com.example.oyun.managers.DailyTaskManager
import com.example.oyun.managers.SoundManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

// --- EKranın Bütün Durumunu Tutan Veri Paketi ---
// Bu, "Single Source of Truth" prensibidir. Orkestranın tek bir notası vardır.
data class GameUiState(
    val currentQuestion: Question? = null,
    val score: Int = 0,
    val lives: Int = 3,
    val currentLevel: Int = 1,
    val questionsAnsweredInLevel: Int = 0,
    val jokerInventory: JokerInventory = JokerInventory(0, 0, 0),
    val isLoading: Boolean = true,
    val isAnswered: Boolean = false // Kullanıcının cevap verip vermediğini tutar
)

// --- Joker Envanteri için Veri Paketi ---
data class JokerInventory(val fiftyFifty: Int, val skip: Int, val gainLife: Int)


@HiltViewModel
class GameViewModel @Inject constructor(
    private val repository: QuestionRepository,
    private val achievementManager: AchievementManager,
    private val dailyTaskManager: DailyTaskManager,
    private val soundManager: SoundManager, // Ses Yöneticisi eklendi
    private val prefs: SharedPreferences
) : ViewModel() {

    // --- DURUM VE OLAY YAYINCILARI ---
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _gameOverEvent = MutableStateFlow<Event<Unit>?>(null)
    val gameOverEvent: StateFlow<Event<Unit>?> = _gameOverEvent.asStateFlow()

    private val _levelUpEvent = MutableStateFlow<Event<Int>?>(null)
    val levelUpEvent: StateFlow<Event<Int>?> = _levelUpEvent.asStateFlow()

    // --- DAHİLİ DURUM DEĞİŞKENLERİ ---
    private var correctAnswerStreak = 0
    private var usedJokersInThisLevel = false
    private val questionsPerLevel = 10
    private val maxLives = 5
    private lateinit var activeUser: String

    // --- OYUN MANTIĞI ---

    /**
     * Oyunu başlatır veya devam ettirir
     */
    fun startGame(startLevel: Int, isNewGame: Boolean = true) {
        activeUser = prefs.getString("last_active_user", "Misafir") ?: "Misafir"
        
        // Joker sayılarını yükle
        val initialJokers = JokerInventory(
            prefs.getInt("profile_${activeUser}_joker_fiftyfifty_count", 0),
            prefs.getInt("profile_${activeUser}_joker_skip_count", 0),
            prefs.getInt("profile_${activeUser}_joker_gainlife_count", 0)
        )
        
        // Yeni oyun mu yoksa devam mı?
        val initialScore = if (isNewGame) 0 else prefs.getInt("profile_${activeUser}_score", 0)
        val initialLives = if (isNewGame) 3 else prefs.getInt("profile_${activeUser}_lives", 3)
        val initialQuestionsAnswered = if (isNewGame) 0 else prefs.getInt("profile_${activeUser}_questions_answered", 0)
        
        repository.loadQuestionsForLevel(startLevel)
        correctAnswerStreak = 0
        usedJokersInThisLevel = false

        _uiState.update {
            it.copy(
                currentLevel = startLevel,
                score = initialScore,
                lives = initialLives,
                questionsAnsweredInLevel = initialQuestionsAnswered,
                jokerInventory = initialJokers,
                isLoading = false
            )
        }
        
        if (isNewGame) {
            prefs.edit().putInt("profile_${activeUser}_current_level", startLevel).apply()
            saveGameState()
        }
        
        loadNextQuestion()
    }

    fun loadNextQuestion() {
        val nextQuestion = repository.getNextQuestion()
        if (nextQuestion != null) {
            _uiState.update { it.copy(currentQuestion = nextQuestion, isAnswered = false) }
        } else {
            // Oyun Bitti (Seviye Bitti ama Level Up tetiklenmediyse son)
            soundManager.playGameOver()
            _gameOverEvent.value = Event(Unit)
        }
    }

    fun handleAnswer(isCorrect: Boolean) {
        if (_uiState.value.isAnswered) return

        _uiState.update { it.copy(isAnswered = true) }

        var newScore = _uiState.value.score
        var newLives = _uiState.value.lives
        var newQuestionsAnswered = _uiState.value.questionsAnsweredInLevel

        if (isCorrect) {
            soundManager.playCorrectAnswer() // ✅ SES
            newScore += 10
            correctAnswerStreak++
            newQuestionsAnswered++
            achievementManager.checkStreakAchievement(correctAnswerStreak)
            dailyTaskManager.onCorrectAnswer()
        } else {
            soundManager.playWrongAnswer() // ❌ SES
            newLives -= 1
            correctAnswerStreak = 0
        }

        _uiState.update {
            it.copy(
                score = newScore,
                lives = newLives,
                questionsAnsweredInLevel = newQuestionsAnswered
            )
        }
        
        saveGameState()

        if (newLives <= 0) {
            soundManager.playGameOver() // 💀 SES
            _gameOverEvent.value = Event(Unit)
        } else {
            checkLevelUp()
        }
    }

    private fun checkLevelUp() {
        if (_uiState.value.questionsAnsweredInLevel >= questionsPerLevel) {
            val newLevel = _uiState.value.currentLevel + 1
            if (repository.hasQuestionsForLevel(newLevel)) {
                soundManager.playLevelUp() // 🆙 SES
                
                achievementManager.checkLevelCompletedNoJokers(!usedJokersInThisLevel)
                achievementManager.checkFirstLevel()
                repository.loadQuestionsForLevel(newLevel)
                usedJokersInThisLevel = false

                _uiState.update {
                    it.copy(
                        currentLevel = newLevel,
                        questionsAnsweredInLevel = 0
                    )
                }
                
                prefs.edit().putInt("profile_${activeUser}_current_level", newLevel).apply()
                saveGameState()
                
                _levelUpEvent.value = Event(newLevel)
            } else {
                soundManager.playGameOver() // 🏁 SES (Oyun tamamen bitti)
                _gameOverEvent.value = Event(Unit)
            }
        }
    }

    fun useFiftyFiftyJoker() {
        val currentJokers = _uiState.value.jokerInventory
        if (currentJokers.fiftyFifty > 0) {
            soundManager.playJokerUse() // 🃏 SES
            _uiState.update { it.copy(jokerInventory = currentJokers.copy(fiftyFifty = currentJokers.fiftyFifty - 1)) }
            usedJokersInThisLevel = true
            saveJokerCounts()
        }
    }

    fun useSkipJoker() {
        val currentJokers = _uiState.value.jokerInventory
        if (currentJokers.skip > 0) {
            soundManager.playJokerUse() // 🃏 SES
            _uiState.update {
                it.copy(
                    jokerInventory = currentJokers.copy(skip = currentJokers.skip - 1),
                    questionsAnsweredInLevel = it.questionsAnsweredInLevel + 1,
                    isAnswered = true
                )
            }
            usedJokersInThisLevel = true
            saveJokerCounts()
            saveGameState()
            checkLevelUp()
        }
    }

    fun useGainLifeJoker() {
        val currentJokers = _uiState.value.jokerInventory
        if (currentJokers.gainLife > 0) {
            soundManager.playJokerUse() // 🃏 SES
            _uiState.update { it.copy(jokerInventory = currentJokers.copy(gainLife = currentJokers.gainLife - 1)) }
            usedJokersInThisLevel = true
            saveJokerCounts()
        }
    }

    fun returnGainLifeJoker() {
        // İade durumunda ses çalmaya gerek yok
        val currentJokers = _uiState.value.jokerInventory
        _uiState.update { it.copy(jokerInventory = currentJokers.copy(gainLife = currentJokers.gainLife + 1)) }
        saveJokerCounts()
    }

    fun addLife() {
        soundManager.playLevelUp() // Can kazanma sesi olarak level up sesi kullanılabilir veya ayrı ses
        _uiState.update { it.copy(lives = (it.lives + 1).coerceAtMost(maxLives)) }
        saveGameState()
    }
    
    // ... rest of the file ...
    // Note: private methods saveJokerCounts and saveGameState are below, they are good.
    // I am including them in replacement content to be safe or rely on StartLine/EndLine logic.
    // I will use StartLine/EndLine to replace standard methods.
    
    private fun saveJokerCounts() {
        val jokers = _uiState.value.jokerInventory
        prefs.edit().apply {
            putInt("profile_${activeUser}_joker_fiftyfifty_count", jokers.fiftyFifty)
            putInt("profile_${activeUser}_joker_skip_count", jokers.skip)
            putInt("profile_${activeUser}_joker_gainlife_count", jokers.gainLife)
            apply()
        }
    }
    
    private fun saveGameState() {
        val state = _uiState.value
        prefs.edit().apply {
            putInt("profile_${activeUser}_score", state.score)
            putInt("profile_${activeUser}_lives", state.lives)
            putInt("profile_${activeUser}_questions_answered", state.questionsAnsweredInLevel)
            apply()
        }
    }

    class Event<out T>(private val content: T) {
        private var hasBeenHandled = false
        fun getContentIfNotHandled(): T? = if (hasBeenHandled) null else {
            hasBeenHandled = true
            content
        }
    }
}
