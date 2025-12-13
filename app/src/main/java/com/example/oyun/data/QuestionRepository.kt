package com.example.oyun.data

import javax.inject.Inject
import javax.inject.Singleton

// BU SINIF, VERİ İLE OYUN MANTIĞI ARASINDAKİ KÖPRÜDÜR.
@Singleton
class QuestionRepository @Inject constructor(
    private val questionManager: QuestionManager // Depocu, sadece kütüphaneciyi tanır.
) {
    private var currentLevelQuestions: MutableList<Question> = mutableListOf()
    private var currentQuestionIndex = -1

    fun loadQuestionsForLevel(level: Int) {
        // Kütüphaneciden soruları alır, karıştırır ve bir kenara koyar.
        currentLevelQuestions = questionManager.getQuestionsForLevel(level).shuffled().toMutableList()
        currentQuestionIndex = -1 // Sayacı sıfırlar
    }

    fun getNextQuestion(): Question? {
        // Sıradaki soruyu verir ve cevaplarını karıştırır
        return if (currentQuestionIndex + 1 < currentLevelQuestions.size) {
            currentQuestionIndex++
            // Her soru gösteriminde cevapları karıştır
            currentLevelQuestions[currentQuestionIndex].shuffled()
        } else {
            null
        }
    }

    fun hasQuestionsForLevel(level: Int): Boolean {
        return questionManager.getQuestionsForLevel(level).isNotEmpty()
    }

    fun getRandomJokerQuestion(): JokerQuestion? {
        return questionManager.getRandomJokerQuestion()
    }
}