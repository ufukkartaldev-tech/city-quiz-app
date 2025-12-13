package com.example.oyun.domain

/**
 * Oyunun hangi durumda olduğunu belirtir
 */
enum class GameState {
    NORMAL_QUESTION,    // Normal soru soruluyor
    JOKER_CHOICE,       // Joker seçim ekranı gösteriliyor
    JOKER_QUESTION,     // Joker sorusu soruluyor
    RESULT_SHOWING      // Sonuç gösteriliyor
}