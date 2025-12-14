package com.example.oyun.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.oyun.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented UI tests for GameActivity
 * Tests game UI and interactions
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class GameActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(GameActivity::class.java)

    @Test
    fun gameActivity_displaysQuestionText() {
        // Verify question text is displayed
        onView(withId(R.id.questionText))
            .check(matches(isDisplayed()))
    }

    @Test
    fun gameActivity_displaysAllAnswerOptions() {
        // Verify all 4 answer buttons are displayed
        onView(withId(R.id.optionA))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.optionB))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.optionC))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.optionD))
            .check(matches(isDisplayed()))
    }

    @Test
    fun gameActivity_displaysScoreAndLives() {
        // Verify score display
        onView(withId(R.id.scoreText))
            .check(matches(isDisplayed()))
        
        // Verify lives display
        onView(withId(R.id.livesText))
            .check(matches(isDisplayed()))
    }

    @Test
    fun gameActivity_displaysJokerButtons() {
        // Verify all joker buttons are displayed
        onView(withId(R.id.jokerFiftyFifty))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.jokerSkip))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.jokerGainLife))
            .check(matches(isDisplayed()))
    }

    @Test
    fun gameActivity_displaysProgressBar() {
        // Verify progress bar is displayed
        onView(withId(R.id.progressBar))
            .check(matches(isDisplayed()))
    }

    @Test
    fun clickingAnswerOption_triggersAnswerCheck() {
        // Click an answer option
        onView(withId(R.id.optionA))
            .perform(click())
        
        // Note: Add verification for answer feedback
        // This might require IdlingResource for animations
    }

    @Test
    fun clickingPauseButton_showsPauseDialog() {
        // Click pause button
        onView(withId(R.id.pauseButton))
            .perform(click())
        
        // Verify pause dialog is shown
        // Note: Add dialog verification
    }

    @Test
    fun jokerButtons_areClickable() {
        // Verify joker buttons are clickable
        onView(withId(R.id.jokerFiftyFifty))
            .check(matches(isClickable()))
        
        onView(withId(R.id.jokerSkip))
            .check(matches(isClickable()))
        
        onView(withId(R.id.jokerGainLife))
            .check(matches(isClickable()))
    }
}
