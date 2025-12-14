package com.example.oyun.data

/**
 * Soru Kategorileri
 */
enum class QuestionCategory {
    GEOGRAPHY,      // Coğrafya
    HISTORY,        // Tarih
    CULTURE,        // Kültür
    SPORTS,         // Spor
    GENERAL,        // Genel Kültür
    SCIENCE,        // Bilim
    ART             // Sanat
}

/**
 * Zorluk Seviyeleri
 */
enum class QuestionDifficulty {
    EASY,           // Kolay
    MEDIUM,         // Orta
    HARD,           // Zor
    EXPERT          // Uzman
}

/**
 * Genişletilmiş Soru Modeli
 */
data class ExtendedQuestion(
    val id: Int,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswer: String,
    val imageName: String,
    val level: Int,
    
    // Yeni alanlar
    val category: QuestionCategory = QuestionCategory.GEOGRAPHY,
    val difficulty: QuestionDifficulty = QuestionDifficulty.MEDIUM,
    val explanation: String = "",           // Cevap açıklaması
    val tags: List<String> = emptyList(),   // Etiketler (örn: ["istanbul", "boğaz"])
    val points: Int = 10,                   // Soru puanı (zorluk bazlı)
    val timeLimit: Int = 30,                // Saniye cinsinden süre
    val isVerified: Boolean = true,         // Moderasyon durumu
    val authorId: String = "system",        // Soru yazarı
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Soru İstatistikleri
 */
data class QuestionStats(
    val questionId: Int,
    val totalAttempts: Int = 0,
    val correctAttempts: Int = 0,
    val averageTime: Double = 0.0,
    val skipCount: Int = 0,
    val fiftyFiftyUsed: Int = 0
) {
    val successRate: Double
        get() = if (totalAttempts > 0) {
            (correctAttempts.toDouble() / totalAttempts) * 100
        } else 0.0
}

/**
 * Kullanıcı Sorusu (Community Questions)
 */
data class UserSubmittedQuestion(
    val id: String = "",
    val question: ExtendedQuestion,
    val submittedBy: String = "",
    val submittedByUsername: String = "",
    val submittedAt: Long = System.currentTimeMillis(),
    val status: QuestionStatus = QuestionStatus.PENDING,
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val reports: Int = 0,
    val moderatorNotes: String = ""
)

enum class QuestionStatus {
    PENDING,        // Onay bekliyor
    APPROVED,       // Onaylandı
    REJECTED,       // Reddedildi
    FLAGGED         // İşaretlendi
}

/**
 * Soru Paketi (Question Pack)
 */
data class QuestionPack(
    val id: String,
    val name: String,
    val description: String,
    val category: QuestionCategory,
    val difficulty: QuestionDifficulty,
    val questionCount: Int,
    val iconUrl: String = "",
    val isPremium: Boolean = false,
    val price: Int = 0,                     // Joker cinsinden fiyat
    val isUnlocked: Boolean = false
)

/**
 * Günlük Soru (Daily Challenge)
 */
data class DailyChallenge(
    val date: String,                       // YYYY-MM-DD formatında
    val question: ExtendedQuestion,
    val bonusPoints: Int = 50,
    val bonusJokers: Int = 1,
    val expiresAt: Long
)
