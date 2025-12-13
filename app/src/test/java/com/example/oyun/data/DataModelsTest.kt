package com.example.oyun.data

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/**
 * QuestionData ve LevelQuestions veri modellerinin unit testleri
 */
class DataModelsTest {

    @Test
    fun `Question model should create correctly`() {
        // Given
        val question = Question(
            imageResId = 123,
            questionText = "Test sorusu?",
            options = listOf("A", "B", "C", "D"),
            correctAnswerIndex = 2,
            theme = "Test"
        )

        // Then
        assertThat(question.questionText).isEqualTo("Test sorusu?")
        assertThat(question.options).hasSize(4)
        assertThat(question.correctAnswerIndex).isEqualTo(2)
        assertThat(question.theme).isEqualTo("Test")
    }

    @Test
    fun `Question model should have default theme`() {
        // Given
        val question = Question(
            questionText = "Test?",
            options = listOf("A", "B"),
            correctAnswerIndex = 0
        )

        // Then
        assertThat(question.theme).isEqualTo("Genel")
    }

    @Test
    fun `LevelQuestions should contain correct level and questions`() {
        // Given
        val questionData = QuestionData(
            questionText = "Test?",
            options = listOf("A", "B", "C", "D"),
            correctAnswerIndex = 1,
            imageName = "test.jpg",
            theme = "TestTheme"
        )
        val levelQuestions = LevelQuestions(
            level = 5,
            timeLimitSeconds = 30,
            questions = listOf(questionData)
        )

        // Then
        assertThat(levelQuestions.level).isEqualTo(5)
        assertThat(levelQuestions.timeLimitSeconds).isEqualTo(30)
        assertThat(levelQuestions.questions).hasSize(1)
        assertThat(levelQuestions.questions[0].questionText).isEqualTo("Test?")
    }

    @Test
    fun `JokerQuestion should contain question and correct answers`() {
        // Given
        val jokerQuestion = JokerQuestion(
            questionText = "Hangi şehir?",
            correctAnswers = listOf("İstanbul", "Ankara", "İzmir")
        )

        // Then
        assertThat(jokerQuestion.questionText).isEqualTo("Hangi şehir?")
        assertThat(jokerQuestion.correctAnswers).hasSize(3)
        assertThat(jokerQuestion.correctAnswers).contains("İstanbul")
    }

    @Test
    fun `DailyTask should track progress correctly`() {
        // Given
        val task = DailyTask(
            id = 1,
            title = "Test Görevi",
            description = "Test açıklaması",
            progress = 5,
            maxProgress = 10,
            reward = "100 XP",
            isCompleted = false
        )

        // Then
        assertThat(task.progress).isEqualTo(5)
        assertThat(task.maxProgress).isEqualTo(10)
        assertThat(task.isCompleted).isFalse()
    }

    @Test
    fun `Achievement should have correct properties`() {
        // Given
        val achievement = Achievement(
            id = "test_achievement",
            title = "Test Başarımı",
            description = "Test açıklaması",
            iconEmoji = "🏆",
            isUnlocked = true
        )

        // Then
        assertThat(achievement.id).isEqualTo("test_achievement")
        assertThat(achievement.title).isEqualTo("Test Başarımı")
        assertThat(achievement.isUnlocked).isTrue()
        assertThat(achievement.iconEmoji).isEqualTo("🏆")
    }

    @Test
    fun `GameRoom should initialize with default values`() {
        // Given
        val gameRoom = GameRoom(
            roomId = "room123",
            hostId = "host123",
            hostName = "Host Player"
        )

        // Then
        assertThat(gameRoom.roomId).isEqualTo("room123")
        assertThat(gameRoom.status).isEqualTo("WAITING")
        assertThat(gameRoom.hostScore).isEqualTo(0)
        assertThat(gameRoom.guestScore).isEqualTo(0)
        assertThat(gameRoom.hostFinished).isFalse()
        assertThat(gameRoom.guestFinished).isFalse()
    }

    @Test
    fun `MultiplayerQuestion should create correctly`() {
        // Given
        val mpQuestion = MultiplayerQuestion(
            questionText = "Multiplayer soru?",
            options = listOf("A", "B", "C", "D"),
            correctAnswerIndex = 2
        )

        // Then
        assertThat(mpQuestion.questionText).isEqualTo("Multiplayer soru?")
        assertThat(mpQuestion.options).hasSize(4)
        assertThat(mpQuestion.correctAnswerIndex).isEqualTo(2)
    }
}
