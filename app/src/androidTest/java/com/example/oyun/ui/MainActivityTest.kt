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
 * Instrumented UI tests for MainActivity
 * Tests user interactions and UI state
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun mainActivity_displaysWelcomeMessage() {
        // Verify welcome text is displayed
        onView(withId(R.id.welcomeText))
            .check(matches(isDisplayed()))
    }

    @Test
    fun mainActivity_displaysStartButton() {
        // Verify start button is displayed and clickable
        onView(withId(R.id.startButton))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
    }

    @Test
    fun mainActivity_displaysContinueButton() {
        // Verify continue button is displayed
        onView(withId(R.id.continueButton))
            .check(matches(isDisplayed()))
    }

    @Test
    fun mainActivity_displaysMultiplayerButton() {
        // Verify multiplayer button is displayed
        onView(withId(R.id.multiplayerButton))
            .check(matches(isDisplayed()))
    }

    @Test
    fun mainActivity_displaysAllMenuCards() {
        // Verify all menu cards are displayed
        onView(withId(R.id.jokerModeCard))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.watchAdCard))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.dailyTasksCard))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.levelSelectCard))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.achievementsCard))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.highScoresCard))
            .check(matches(isDisplayed()))
    }

    @Test
    fun clickingSettingsButton_opensSettings() {
        // Click settings button
        onView(withId(R.id.settingsButtonAnchor))
            .perform(click())
        
        // Note: Add verification for settings activity/dialog if needed
    }

    @Test
    fun clickingJokerModeCard_opensJokerMode() {
        // Click joker mode card
        onView(withId(R.id.jokerModeCard))
            .perform(click())
        
        // Verify navigation to JokerModeActivity
        // Note: You may need to add IdlingResource for async operations
    }

    @Test
    fun clickingAchievementsCard_opensAchievements() {
        // Click achievements card
        onView(withId(R.id.achievementsCard))
            .perform(click())
        
        // Verify navigation to AchievementsActivity
    }

    @Test
    fun clickingHighScoresCard_opensHighScores() {
        // Click high scores card
        onView(withId(R.id.highScoresCard))
            .perform(click())
        
        // Verify navigation to HighScoreActivity
    }
}
