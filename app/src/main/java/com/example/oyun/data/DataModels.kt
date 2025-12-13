package com.example.oyun.data

// Ana Question sınıfı
data class Question(
    val imageResId: Int = 0,
    val questionText: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val theme: String = "Genel" // Varsayılan theme
) {
    /**
     * Cevapları karıştırır ve yeni doğru cevap index'ini hesaplar
     * Bu sayede her soru gösteriminde cevaplar farklı sırada olur
     */
    fun shuffled(): Question {
        // Cevapları orijinal index'leriyle eşleştir
        val indexedOptions = options.mapIndexed { index, option -> 
            index to option 
        }
        
        // Karıştır
        val shuffledOptions = indexedOptions.shuffled()
        
        // Doğru cevabın yeni pozisyonunu bul
        val newCorrectIndex = shuffledOptions.indexOfFirst { 
            it.first == correctAnswerIndex 
        }
        
        // Yeni Question objesi döndür
        return Question(
            imageResId = imageResId,
            questionText = questionText,
            options = shuffledOptions.map { it.second },
            correctAnswerIndex = newCorrectIndex,
            theme = theme
        )
    }
}

// JSON'dan okunan veri için - ÖNEMLİ: timeLimitSeconds olmalı, time değil
data class LevelQuestions(
    val level: Int,
    val timeLimitSeconds: Int? = null, // JSON'daki field adıyla eşleşmeli
    val questions: List<QuestionData>
)

data class QuestionData(
    val questionText: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val imageName: String = "",
    val theme: String? = null // JSON'da theme yok, nullable
)

// Joker sorular için
data class JokerQuestion(
    val questionText: String,
    val correctAnswers: List<String>
)

data class CityJokerData(
    val city: String,
    val joker_questions: List<JokerQuestion>
)

// Günlük görevler için
data class DailyTask(
    val id: Int,
    val title: String,
    val description: String,
    val progress: Int,
    val maxProgress: Int,
    val reward: String,
    val isCompleted: Boolean
)

// Günlük görev verileri
object DailyTaskData {
    val dailyTasks = listOf(
        DailyTask(
            id = 1,
            title = "İlk Görev",
            description = "Bugün 1 oyun tamamla",
            progress = 0,
            maxProgress = 1,
            reward = "50 XP",
            isCompleted = false
        ),
        DailyTask(
            id = 2,
            title = "Seri Başlangıcı",
            description = "3 soru üst üste doğru cevapla",
            progress = 0,
            maxProgress = 3,
            reward = "100 XP",
            isCompleted = false
        ),
        DailyTask(
            id = 3,
            title = "Meraklı",
            description = "10 farklı soruyu çöz",
            progress = 0,
            maxProgress = 10,
            reward = "150 XP",
            isCompleted = false
        )
    )
}

// Başarımlar için
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val isUnlocked: Boolean = false
)

// Başarım kategorileri
enum class AchievementCategory {
    FIRST_STEPS,    // İlk adımlar
    QUESTIONS,      // Soru bazlı
    LEVELS,         // Level bazlı
    JOKERS,         // Joker bazlı
    EXPERT          // Uzman seviye
}

// Başarım verileri
object AchievementData {
    val achievements = listOf(
        Achievement(
            id = "first_game",
            title = "İlk Adım",
            description = "İlk oyununu tamamladın",
            iconEmoji = "🎮"
        ),
        Achievement(
            id = "first_level",
            title = "Seviye Atlama",
            description = "İlk seviyeni tamamladın",
            iconEmoji = "🏁"
        ),
        Achievement(
            id = "questions_25",
            title = "Meraklı",
            description = "25 soruyu doğru cevapladın",
            iconEmoji = "🤔"
        ),
        Achievement(
            id = "questions_100",
            title = "Bilgin",
            description = "100 soruyu doğru cevapladın",
            iconEmoji = "🧠"
        ),
        Achievement(
            id = "streak_5",
            title = "Seri Katili",
            description = "5 soru üst üste doğru cevapladın",
            iconEmoji = "🔥"
        ),
        Achievement(
            id = "level_no_joker",
            title = "Saf Yetenek",
            description = "Hiç joker kullanmadan bir seviyeyi tamamladın",
            iconEmoji = "💪"
        ),
        Achievement(
            id = "reach_level_5",
            title = "İlerleyici",
            description = "5. seviyeye ulaştın",
            iconEmoji = "🚀"
        ),
        Achievement(
            id = "reach_level_8",
            title = "Şampiyon",
            description = "8. seviyeye ulaştın",
            iconEmoji = "👑"
        ),
        Achievement(
            id = "games_10",
            title = "Oyuncu",
            description = "10 oyun oynadın",
            iconEmoji = "🎯"
        ),
        Achievement(
            id = "perfect_game",
            title = "Mükemmeliyetçi",
            description = "Hiç joker kullanmadan tüm soruları çözdün",
            iconEmoji = "⭐"
        )
    )
}

// --- MULTIPLAYER MODELLERİ ---

data class GameRoom(
    val roomId: String = "",
    val hostId: String = "",
    val hostName: String = "",
    val guestId: String? = null,
    val guestName: String? = null,
    val status: String = "WAITING", // WAITING, PLAYING, FINISHED
    val hostScore: Int = 0,
    val guestScore: Int = 0,
    val hostFinished: Boolean = false, // Host tüm soruları bitirdi mi?
    val guestFinished: Boolean = false, // Guest tüm soruları bitirdi mi?
    val roomCode: String? = null, // 6 haneli oda kodu
    val lastEmoji: String? = null, // Format: "userId|emoji"
    val questionSeed: Long = 0L // Soru senkronizasyonu için seed
)

data class FriendRequest(
    val requestId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val receiverId: String = "",
    val status: String = "PENDING", // PENDING, ACCEPTED, REJECTED
    val timestamp: Long = System.currentTimeMillis()
)

data class UserFriend(
    val userId: String = "",
    val userName: String = "",
    val addedAt: Long = System.currentTimeMillis()
)

data class MultiplayerQuestion(
    val questionText: String = "",
    val options: List<String> = emptyList(),
    val correctAnswerIndex: Int = 0
) {
    /**
     * Verilen Random objesine göre cevapları karıştırır
     * Bu sayede her iki oyuncuda da aynı sıralama olur
     */
    fun shuffled(random: java.util.Random): MultiplayerQuestion {
        val indexedOptions = options.mapIndexed { index, option -> 
            index to option 
        }
        // Random objesini kullanarak karıştır
        val shuffledOptions = indexedOptions.shuffled(random)
        val newCorrectIndex = shuffledOptions.indexOfFirst { 
            it.first == correctAnswerIndex 
        }
        
        return MultiplayerQuestion(
            questionText = questionText,
            options = shuffledOptions.map { it.second },
            correctAnswerIndex = newCorrectIndex
        )
    }
}
