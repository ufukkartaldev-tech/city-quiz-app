package com.example.oyun.ui.multiplayer

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import com.example.oyun.R
import com.example.oyun.data.MultiplayerQuestion
import com.example.oyun.databinding.ActivityMultiplayerGameBinding

/**
 * Multiplayer UI güncellemelerini yöneten sınıf
 */
class MultiplayerUIManager(
    private val context: Context,
    private val binding: ActivityMultiplayerGameBinding
) {

    fun showWaitingForOpponent() {
        binding.questionText.text = context.getString(R.string.waiting_opponent)
        binding.timerText.visibility = View.GONE
        setAnswerButtonsEnabled(false)
    }

    fun displayQuestion(question: MultiplayerQuestion, questionIndex: Int) {
        binding.questionText.text = "${questionIndex + 1}. ${question.questionText}"
        binding.answerButton1.text = question.options[0]
        binding.answerButton2.text = question.options[1]
        binding.answerButton3.text = question.options[2]
        binding.answerButton4.text = question.options[3]
        
        binding.timerText.visibility = View.VISIBLE
        setAnswerButtonsEnabled(true)
        resetButtonVisibility()
        clearStatus()
    }

    fun showCorrectAnswer(score: Int) {
        // binding.statusText.text = context.getString(R.string.status_correct, score)
        // binding.statusText.setTextColor(
        //     ContextCompat.getColor(context, android.R.color.holo_green_dark)
        // )
    }

    fun showWrongAnswer() {
        // binding.statusText.text = context.getString(R.string.status_wrong)
        // binding.statusText.setTextColor(
        //     ContextCompat.getColor(context, android.R.color.holo_red_dark)
        // )
    }

    fun showGameFinished() {
        binding.questionText.text = context.getString(R.string.all_questions_finished)
        hideAnswerButtons()
        binding.timerText.visibility = View.GONE
        // binding.statusText.text = context.getString(R.string.status_waiting)
    }

    fun updatePlayerScores(
        myName: String,
        myScore: Int,
        opponentName: String,
        opponentScore: Int
    ) {
        // binding.myName.text = myName
        // binding.myScoreText.text = myScore.toString()
        // binding.opponentName.text = opponentName
        // binding.opponentScoreText.text = opponentScore.toString()
    }

    fun setAnswerButtonsEnabled(enabled: Boolean) {
        binding.answerButton1.isEnabled = enabled
        binding.answerButton2.isEnabled = enabled
        binding.answerButton3.isEnabled = enabled
        binding.answerButton4.isEnabled = enabled
    }

    fun areAnswerButtonsEnabled(): Boolean {
        return binding.answerButton1.isEnabled
    }

    private fun hideAnswerButtons() {
        binding.answerButton1.visibility = View.INVISIBLE
        binding.answerButton2.visibility = View.INVISIBLE
        binding.answerButton3.visibility = View.INVISIBLE
        binding.answerButton4.visibility = View.INVISIBLE
    }

    private fun resetButtonVisibility() {
        binding.answerButton1.visibility = View.VISIBLE
        binding.answerButton2.visibility = View.VISIBLE
        binding.answerButton3.visibility = View.VISIBLE
        binding.answerButton4.visibility = View.VISIBLE
    }

    private fun clearStatus() {
        // binding.statusText.text = ""
    }
}
