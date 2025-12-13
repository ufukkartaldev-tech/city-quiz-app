package com.example.oyun.ui.multiplayer

import com.example.oyun.data.MultiplayerQuestion

/**
 * Multiplayer oyun soruları
 */
object MultiplayerQuestions {
    
    val questionSet = listOf(
        MultiplayerQuestion(
            "Türkiye'nin başkenti neresidir?",
            listOf("İstanbul", "Ankara", "İzmir", "Bursa"),
            1
        ),
        MultiplayerQuestion(
            "Hangi gezegen Kızıl Gezegen olarak bilinir?",
            listOf("Venüs", "Mars", "Jüpiter", "Satürn"),
            1
        ),
        MultiplayerQuestion(
            "Su kaç derecede kaynar?",
            listOf("90", "100", "110", "120"),
            1
        ),
        MultiplayerQuestion(
            "En büyük okyanus hangisidir?",
            listOf("Atlas", "Hint", "Pasifik", "Arktik"),
            2
        ),
        MultiplayerQuestion(
            "Fatih Sultan Mehmet İstanbul'u kaç yılında fethetti?",
            listOf("1453", "1071", "1299", "1923"),
            0
        ),
        MultiplayerQuestion(
            "İstiklal Marşı'nın şairi kimdir?",
            listOf("Orhan Veli", "Nazım Hikmet", "Mehmet Akif Ersoy", "Cemal Süreya"),
            2
        ),
        MultiplayerQuestion(
            "Bir gün kaç saattir?",
            listOf("12", "24", "48", "60"),
            1
        ),
        MultiplayerQuestion(
            "Türkiye'nin en yüksek dağı hangisidir?",
            listOf("Erciyes", "Ağrı", "Uludağ", "Süphan"),
            1
        ),
        MultiplayerQuestion(
            "Hangi hayvan 'Ormanlar Kralı' olarak bilinir?",
            listOf("Kaplan", "Fil", "Aslan", "Zürafa"),
            2
        ),
        MultiplayerQuestion(
            "Futbol maçları kaç dakika sürer?",
            listOf("45", "60", "90", "100"),
            2
        )
    )
}
